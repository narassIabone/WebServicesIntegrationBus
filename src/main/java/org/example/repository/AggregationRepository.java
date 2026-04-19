package org.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.model.AggregationResult;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AggregationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AggregationResult upsertFragment(UUID correlationId, String payload, Map<String, Object> context, int expectedCount) {
        String sql = """
            INSERT INTO aggregation_storage (correlation_id, expected_count, received_count, payloads, contexts)
            VALUES (:id, :expected, 1, :payload::jsonb, :context::jsonb) 
            ON CONFLICT (correlation_id) 
            DO UPDATE SET 
                received_count = aggregation_storage.received_count + 1,
                payloads = aggregation_storage.payloads || :payload::jsonb,
                contexts = aggregation_storage.contexts || :context::jsonb,
                updated_at = CURRENT_TIMESTAMP
            RETURNING received_count, expected_count, payloads::text, contexts::text
        """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", correlationId)
                    .addValue("expected", expectedCount)
                    .addValue("payload", payload)
                    .addValue("context", objectMapper.writeValueAsString(context));

            return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> new AggregationResult(
                    rs.getInt("received_count"),
                    rs.getInt("expected_count"),
                    rs.getString("payloads"),
                    rs.getString("contexts")
            ));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения фрагмента", e);
        }
    }

    public void deleteByCorrelationId(UUID correlationId) {
        jdbcTemplate.update("DELETE FROM aggregation_storage WHERE correlation_id = :id",
                new MapSqlParameterSource("id", correlationId));
    }
}