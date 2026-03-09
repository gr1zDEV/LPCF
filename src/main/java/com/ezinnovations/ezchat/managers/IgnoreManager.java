package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.database.repository.IgnoreRepository;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IgnoreManager {

    public enum IgnoreType {
        ALL,
        CHAT,
        MSG,
        MAIL
    }

    private final EzChat plugin;
    private final IgnoreRepository ignoreRepository;
    private final Map<UUID, Map<UUID, IgnoreType>> ignoresByViewer = new ConcurrentHashMap<>();

    public IgnoreManager(final EzChat plugin, final IgnoreRepository ignoreRepository) {
        this.plugin = plugin;
        this.ignoreRepository = ignoreRepository;
    }

    public void load() {
        ignoresByViewer.clear();
        try {
            ignoresByViewer.putAll(ignoreRepository.loadAll());
        } catch (final SQLException exception) {
            plugin.getLogger().severe("Failed to load ignore data from SQLite: " + exception.getMessage());
        }
    }

    public void save() {
        // Persistence is immediate.
    }

    public boolean isIgnoring(final UUID viewer, final UUID sender, final IgnoreType type) {
        final Map<UUID, IgnoreType> viewerIgnores = ignoresByViewer.get(viewer);
        if (viewerIgnores == null) {
            return false;
        }

        final IgnoreType storedType = viewerIgnores.get(sender);
        if (storedType == null) {
            return false;
        }

        return storedType == IgnoreType.ALL || storedType == type;
    }

    public boolean toggleIgnore(final UUID viewer, final UUID target, final IgnoreType type) {
        final Map<UUID, IgnoreType> viewerIgnores = ignoresByViewer.computeIfAbsent(viewer, ignored -> new ConcurrentHashMap<>());
        final IgnoreType current = viewerIgnores.get(target);

        if (current == type) {
            viewerIgnores.remove(target);
            cleanupViewerIfEmpty(viewer, viewerIgnores);
            persistRemoval(viewer, target);
            return false;
        }

        viewerIgnores.put(target, type);
        persistSet(viewer, target, type);
        return true;
    }

    private void persistSet(final UUID viewer, final UUID target, final IgnoreType type) {
        try {
            ignoreRepository.setIgnore(viewer, target, type);
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to persist ignore " + viewer + " -> " + target + ": " + exception.getMessage());
        }
    }

    private void persistRemoval(final UUID viewer, final UUID target) {
        try {
            ignoreRepository.removeIgnore(viewer, target);
        } catch (final SQLException exception) {
            plugin.getLogger().warning("Failed to remove ignore " + viewer + " -> " + target + ": " + exception.getMessage());
        }
    }

    private void cleanupViewerIfEmpty(final UUID viewer, final Map<UUID, IgnoreType> viewerIgnores) {
        if (viewerIgnores.isEmpty()) {
            ignoresByViewer.remove(viewer);
        }
    }
}
