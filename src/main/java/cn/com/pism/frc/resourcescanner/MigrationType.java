/*
 * Copyright 2010-2020 Redgate Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.pism.frc.resourcescanner;

/**
 * Type of migration.
 */
public enum MigrationType {
    /**
     * Schema creation migration.
     */
    SCHEMA(true, false),

    /**
     * Baseline migration.
     */
    BASELINE(true, false),

    /**
     * SQL migrations.
     */
    SQL(false, false),

    /**
     * Undo SQL migrations.
     */
    UNDO_SQL(false, true),

    /**
     * JDBC Java-based migrations.
     */
    JDBC(false, false),

    /**
     * Undo JDBC java-based migrations.
     */
    UNDO_JDBC(false, true),

    /**
     * Spring JDBC Java-based migrations.
     *
     * @deprecated Will be removed in Flyway 7.0. Use JDBC instead.
     */
    @Deprecated
    SPRING_JDBC(false, false),

    /**
     * Undo Spring JDBC java-based migrations.
     *
     * @deprecated Will be removed in Flyway 7.0. Use UNDO_JDBC instead.
     */
    @Deprecated
    UNDO_SPRING_JDBC(false, true),

    /**
     * Migrations using custom MigrationResolvers.
     */
    CUSTOM(false, false),

    /**
     * Undo migrations using custom MigrationResolvers.
     */
    UNDO_CUSTOM(false, true);

    private final boolean synthetic;
    private final boolean undo;

    MigrationType(boolean synthetic, boolean undo) {
        this.synthetic = synthetic;
        this.undo = undo;
    }

    /**
     * @return Whether this is a synthetic migration type, which is only ever present in the schema history table,
     * but never discovered by migration resolvers.
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * @return Whether this is an undo migration, which has undone an earlier migration present in the schema history table.
     */
    public boolean isUndo() {
        return undo;
    }
}