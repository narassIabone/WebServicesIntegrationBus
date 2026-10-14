package org.example.util;

import org.flywaydb.core.Flyway;

public class FlywayMigration {
    public static void main(String[] args){
        Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://localhost:5432/postgres",
                "postgres","postgres").baselineOnMigrate(true).load();
        flyway.migrate();
    }
}
