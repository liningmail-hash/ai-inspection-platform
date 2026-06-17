package com.aiplatform.inspection.repository;

import com.aiplatform.inspection.domain.Device;
import com.aiplatform.inspection.domain.VideoChannel;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("postgres")
public class PostgresDeviceManagementRepository implements DeviceManagementRepository {
    private final JdbcTemplate jdbcTemplate;

    public PostgresDeviceManagementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Device> devices() {
        return jdbcTemplate.query("""
            select id::text, name, source_type, vendor, protocol, endpoint, credential_ref, location,
                   edge_node_ref, status, stream_url, created_at::text, updated_at::text
            from devices
            where deleted_at is null
            order by created_at desc, name asc
            """, (rs, rowNum) -> toDevice(rs));
    }

    @Override
    public Optional<Device> deviceById(String id) {
        List<Device> results = jdbcTemplate.query("""
            select id::text, name, source_type, vendor, protocol, endpoint, credential_ref, location,
                   edge_node_ref, status, stream_url, created_at::text, updated_at::text
            from devices
            where id = ?::uuid and deleted_at is null
            """, (rs, rowNum) -> toDevice(rs), id);
        return results.stream().findFirst();
    }

    @Override
    public Device createDevice(Device device) {
        jdbcTemplate.update("""
            insert into devices(id, name, source_type, vendor, protocol, endpoint, credential_ref, location,
                                edge_node_ref, status, stream_url, created_at, updated_at)
            values (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::timestamptz, ?::timestamptz)
            """, device.id(), device.name(), device.sourceType(), device.vendor(), device.protocol(), device.endpoint(),
            device.credentialRef(), device.location(), device.edgeNodeId(), device.status(), device.streamUrl(),
            device.createdAt(), device.updatedAt());
        return device;
    }

    @Override
    public Optional<Device> updateDevice(String id, Device device) {
        int updated = jdbcTemplate.update("""
            update devices
            set name = ?, source_type = ?, vendor = ?, protocol = ?, endpoint = ?, credential_ref = ?,
                location = ?, edge_node_ref = ?, status = ?, stream_url = ?, updated_at = ?::timestamptz
            where id = ?::uuid and deleted_at is null
            """, device.name(), device.sourceType(), device.vendor(), device.protocol(), device.endpoint(),
            device.credentialRef(), device.location(), device.edgeNodeId(), device.status(), device.streamUrl(),
            device.updatedAt(), id);
        return updated == 0 ? Optional.empty() : deviceById(id);
    }

    @Override
    public Optional<Device> updateDeviceStatus(String id, String status, Instant updatedAt) {
        int updated = jdbcTemplate.update("""
            update devices set status = ?, updated_at = ? where id = ?::uuid and deleted_at is null
            """, status, Timestamp.from(updatedAt), id);
        return updated == 0 ? Optional.empty() : deviceById(id);
    }

    @Override
    public boolean deleteDevice(String id, Instant deletedAt) {
        int updated = jdbcTemplate.update("""
            update devices set status = 'deleted', deleted_at = ?, updated_at = ? where id = ?::uuid and deleted_at is null
            """, Timestamp.from(deletedAt), Timestamp.from(deletedAt), id);
        return updated > 0;
    }

    @Override
    public List<VideoChannel> replaceDeviceChannels(String deviceId, List<VideoChannel> channels, Instant syncedAt) {
        jdbcTemplate.update("delete from video_channels where source_id = ?", deviceId);
        for (VideoChannel channel : channels) {
            jdbcTemplate.update("""
                insert into video_channels(id, device_id, name, channel_no, stream_profile, online, source_type,
                                           source_id, source_name, protocol, stream_url, play_url, ai_enabled,
                                           status, edge_node, latitude, longitude)
                values (?::uuid, null, ?, ?, 'main', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, channel.id(), channel.name(), channel.id(), "online".equals(channel.status()), channel.sourceType(),
                channel.sourceId(), channel.sourceName(), channel.protocol(), channel.streamUrl(), channel.playUrl(),
                channel.aiEnabled(), channel.status(), channel.edgeNode(), channel.latitude(), channel.longitude());
        }
        return channels;
    }

    private Device toDevice(ResultSet rs) throws SQLException {
        return new Device(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("source_type"),
            rs.getString("vendor"),
            rs.getString("protocol"),
            rs.getString("endpoint"),
            rs.getString("credential_ref"),
            rs.getString("location"),
            rs.getString("edge_node_ref"),
            rs.getString("status"),
            rs.getString("stream_url"),
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
    }
}
