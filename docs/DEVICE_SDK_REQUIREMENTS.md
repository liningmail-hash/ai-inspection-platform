# Device SDK Requirements

This draft lists the first set of field information required before real vendor
SDK implementations can replace `MOCK_VENDOR`.

## HIKVISION Cameras

Required placeholders:

- `endpoint`: ISUP/OpenAPI/SDK gateway host and port.
- `username`: SDK login account name.
- `credentialRef`: secret manager key or deployment secret name. Do not store real passwords in code or docs.
- `sdkLibraryPath`: path containing HCNetSDK Java wrapper jar and native libraries.
- `streamMode`: preferred stream mode, such as `rtsp`, `hls`, `flv`, or `webrtc`.
- `callbackBaseUrl`: backend URL reachable by HIKVISION callbacks.

SDK dependencies still needed:

- HCNetSDK Java wrapper jar.
- Matching native libraries for the deployment OS and CPU architecture.
- Vendor SDK documentation for login, channel query, preview, snapshot, and record APIs.

Channel mapping target:

- Device id maps to `VideoChannel.sourceId`.
- Channel id maps to `VideoChannel.id`.
- Channel name maps to `VideoChannel.name`.
- Stream type maps to `VideoChannel.streamName`.
- Vendor protocol maps to `VideoChannel.protocol`.
- Raw vendor stream URL maps to `VideoChannel.rawUrl`.
- Playable platform stream URL maps to `VideoChannel.playUrl`.
- Online flag maps to `VideoChannel.online` and `VideoChannel.status`.
- Location maps to `VideoChannel.latitude` and `VideoChannel.longitude`.

Current limitation:

- Without the real SDK jar, native libraries, and credentials, the backend
  returns explicit unavailable results and keeps `MOCK_VENDOR` as the default
  demonstration mode.

Real access collection status:

- Details are tracked in `docs/REAL_DEVICE_ACCESS_HIKVISION.md`.
- First real pilot should prefer direct IPC/NVR or an existing Hikvision platform
  gateway. Use ISUP only when devices cannot be reached directly.
- Minimum onsite parameters blocking implementation are access mode, endpoint,
  port, credentialRef, channel code, live view permission, snapshot permission,
  recording/playback permission, and event subscription permission.
- SP-REAL-P0-002 status: access mode, platform gateway availability, one real
  channel, and test account permissions are still pending onsite confirmation.
- Programmer A is blocked for real HIKVISION adapter work until channel read,
  live preview, endpoint/port, `vendorDeviceCode`, `vendorChannelCode`,
  `channelNo`, `streamType`, and `credentialRef` are confirmed.
- The onsite team can use section 9 of `docs/REAL_DEVICE_ACCESS_HIKVISION.md`
  as the official parameter request form. Section 10 lists the current blocking
  items for a single-channel real video pilot.
