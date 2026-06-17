# HIKVISION Real Device Access Checklist

Task: SP-REAL-P0-001 / SP-REAL-P0-002

Status: collecting. This document contains no real password, token, customer IP,
or device secret.

Current conclusion for SP-REAL-P0-002:

- Access mode is not yet confirmed by the onsite team.
- HikCentral / iSecure Center / OpenAPI gateway availability is not yet
  confirmed.
- No real single-channel endpoint, channel code, or test account permission has
  been provided yet.
- Single-channel real video pilot is not ready until the checklist in section 7
  is completed.

## 1. Access Mode Decision

Priority for the first real video pilot:

1. Direct IPC/NVR access for one or two cameras.
2. Hikvision platform gateway access when the customer already has HikCentral,
   iSecure Center, Hikvision OpenAPI, or another official gateway.
3. ISUP/EHome inbound registration when devices are behind NAT or 4G/5G and
   cannot be reached directly from the platform.

Recommended first pilot mode:

- Start with direct IPC/NVR or existing platform gateway.
- Do not start with ISUP unless the onsite network requires device-initiated
  registration. ISUP adds platform-side public port, device ID, secret, alarm,
  and stream-server coordination work.

## 2. HIKVISION Modes

### 2.1 Direct IPC / NVR

Use when:

- Camera or NVR is reachable from the edge node or backend network.
- Onsite can provide a test account.
- One-channel real video proof is the immediate target.

Required SDK / protocol:

- HCNetSDK Java wrapper jar.
- Matching native libraries for Linux x86_64 or Windows x64.
- Optional PlayCtrl library when local decoding is required.
- RTSP can be used for video output when only live stream is required.

Expected endpoints and ports:

- SDK endpoint: `<device_ip>:8000` by common Hikvision default, must verify onsite.
- RTSP endpoint: `rtsp://<device_ip>:554/...`, must verify onsite.
- HTTP/ISAPI endpoint: `http://<device_ip>:80` or `https://<device_ip>:443`,
  must verify onsite.

Account permissions to request:

- Login / device information read.
- Channel list read.
- Live view / stream permission.
- Snapshot permission.
- Recording query / playback permission if NVR recording is needed.
- Event or alarm subscription permission if alarms are needed.
- PTZ permission only for PTZ cameras and only if the project needs it.

Channel sync:

- Login through SDK or platform API.
- Query device capabilities and channel list.
- Map each analog/IP channel to `video_channel`.
- Store vendor channel number, channel name, online status, stream type, and
  raw stream identifier.

Video output:

- Preferred for platform playback: edge node pulls RTSP/SDK stream and converts
  to WebRTC, FLV, or HLS.
- Fallback: frontend receives a platform-generated playable URL, not the raw
  camera password-bearing URL.

Snapshot:

- SDK snapshot: use HCNetSDK capture APIs when available.
- RTSP fallback: edge node extracts a frame with FFmpeg/OpenCV.
- ISAPI snapshot can be considered only after confirming device model support.

Recording:

- NVR/device recording query through SDK or platform API.
- Platform evidence recording: edge node cuts short clips from the live stream
  and stores them in MinIO.

Event subscription:

- SDK alarm callback or platform event API, depending on access mode.
- Required callback receiver must be reachable by the SDK process or gateway.

### 2.2 Hikvision Platform Gateway / OpenAPI

Use when:

- Customer already has a Hikvision platform that manages devices and channels.
- The platform can issue app key / app secret or integration account.
- Channel and stream URLs can be obtained from gateway API.

Required SDK / protocol:

- Official platform OpenAPI documentation for the exact product and version.
- AppKey/AppSecret or integration account, stored as `credentialRef`.
- OpenAPI endpoint base URL and API signing rule.

Endpoint placeholders:

- `platformBaseUrl`: `https://<hik-platform-host>:<port>`
- `appKey`: secret, stored outside code.
- `appSecret`: secret, stored outside code.
- `callbackBaseUrl`: `https://<our-domain>/api/vendor/hikvision/callbacks`

Account permissions to request:

- Organization/resource tree read.
- Camera/channel read.
- Live stream URL acquisition.
- Playback URL or recording search.
- Picture capture if platform exposes snapshot API.
- Event subscription and event query.
- PTZ control if needed.

Channel sync:

- Pull organization tree and camera resources from OpenAPI.
- Use platform resource index/code as stable `vendorChannelCode`.
- Keep channel name, organization path, online status, longitude/latitude, and
  stream capability.

Video output:

- Prefer gateway-provided FLV/HLS/WebRTC/RTSP URL if available.
- Otherwise edge node pulls gateway RTSP and republishes WebRTC/FLV/HLS.

Snapshot / recording / event:

- Prefer gateway APIs to avoid direct camera credentials.
- Confirm whether APIs require separate permission packages.

### 2.3 ISUP / EHome Registration

Use when:

- Devices are behind NAT, 4G/5G, or cannot be actively connected by the platform.
- Device can initiate registration to our platform or to Hikvision gateway.

Required SDK / protocol:

- ISUP/EHome SDK package and documentation matching device firmware.
- Registration service, stream service, alarm service, and optional storage
  callback service.

Endpoint placeholders:

- `isupServerHost`: public IP or domain reachable by device.
- `isupRegisterPort`: onsite-confirmed registration port.
- `isupDeviceId`: device/platform registration ID.
- `isupSecret`: stored as `credentialRef`, never in docs.
- `isupAlarmPort`: onsite-confirmed alarm TCP/UDP port.
- `isupStreamPort`: onsite-confirmed stream server port.

Ports to verify onsite:

- Some Hikvision ISUP guides mention ports such as `7660/7661` for ISUP access
  and stream services, `7332/7334` for alarm reception, `554` for RTSP live
  view, `559` for browser WebSocket stream, `10000` for playback stream, and
  `6123` / `27661` for picture or signaling callbacks. These values are not
  universal; confirm against the onsite platform and firmware.

Channel sync:

- Device registers with ID and secret.
- Platform receives device information and channel list from ISUP service.
- Map ISUP device ID + channel number to internal video channel.

Video output:

- ISUP stream service receives or pulls stream, then converts to WebRTC/FLV/HLS.
- Do not expose ISUP service ports broadly unless required by device network.

Snapshot / recording / event:

- Snapshot and event callback depend on ISUP service configuration.
- Recording may be device/NVR-side, platform-side, or edge-side; confirm which
  one is required for phase one.

## 3. Data Fields Needed From Site

### Common

- Customer site name.
- Network topology: camera LAN, edge node LAN, cloud access path.
- Deployment target: edge node, center server, or existing Hikvision gateway.
- Whether the device can be reached directly from the edge node.
- Whether public inbound ports are allowed for ISUP.
- Time sync / NTP policy.

### Direct IPC / NVR

- Device type: IPC or NVR.
- Device model and firmware version.
- Device IP or hostname.
- SDK port.
- RTSP port.
- HTTP/HTTPS port.
- Test account username.
- Credential storage reference name.
- Whether account can read channel list.
- Whether account can preview live video.
- Whether account can capture snapshots.
- Whether account can query recordings and playback.
- Whether account can receive alarms/events.
- Whether PTZ is allowed.

### Platform Gateway / OpenAPI

- Platform product name and version.
- API base URL.
- API authentication method.
- AppKey or integration account name.
- Credential storage reference name.
- Resource tree / camera list API permission.
- Live stream API permission.
- Playback / recording API permission.
- Snapshot API permission.
- Event subscription callback permission.
- Callback URL whitelist requirements.
- HTTPS certificate requirements.

### ISUP / EHome

- Device-side ISUP/EHome support and version.
- Public registration host/domain.
- Registration port.
- Device ID.
- Secret storage reference name.
- Stream service port.
- Alarm service TCP/UDP ports.
- Callback ports.
- Whether router/security group port mapping is available.
- Whether device network can reach our registration service.

## 4. Internal Mapping Proposal

Device fields:

- `sourceType`: `camera`
- `vendor`: `HIKVISION`
- `endpoint`: normalized endpoint, for example `sdk://10.0.1.10:8000`,
  `rtsp://10.0.1.10:554`, `hik-openapi://platform.example.local`, or
  `isup://public.example.com:7660`
- `credentialRef`: secret reference only.
- `edgeNodeId`: edge node responsible for pulling streams.
- `status`: `offline`, `online`, `unavailable`, or `unknown`.

Channel fields:

- `vendorDeviceCode`
- `vendorChannelCode`
- `channelNo`
- `channelName`
- `streamType`: main/sub/third stream.
- `rawUrl`: optional encrypted/internal only.
- `playUrl`: platform playback URL.
- `snapshotSupported`
- `recordingSupported`
- `eventSupported`
- `ptzSupported`

## 5. First Real Pilot Acceptance

Minimum conditions for a single-channel real video pilot:

- A reachable HIKVISION IPC/NVR or platform gateway.
- A test account with channel read and live view permission.
- One confirmed channel code.
- One working video output path: RTSP to edge or gateway stream URL.
- One snapshot method: SDK/API snapshot or FFmpeg frame capture.
- Evidence clip strategy decided: device/NVR playback or edge-side short clip.

If these are not available, developers can still complete CRUD, credentialRef,
channel model, and explicit `unavailable` responses, but cannot claim real video
access is complete.

## 6. Sources Checked

- Hikvision Open Platform page: https://www.hikvision.com/cn/OpenPlatform/
- Hikvision SDK download page: https://www.hikvision.com/us-en/support/download/sdk/
- Hikvision third-party integration overview PDF, which describes ISAPI as a
  REST-style interface for devices and servers.
- Hikvision ISUP access documents and public guide snippets found during
  collection. Port values must be verified against onsite product documentation.

## 7. SP-REAL-P0-002 Site Confirmation Sheet

Fill this sheet with placeholders or secret references only. Do not write real
passwords, real tokens, AppSecret values, or sensitive customer internal IP
details into this repository.

### 7.1 Final Access Mode

| Item | Value | Status | Owner |
| --- | --- | --- | --- |
| Final access mode | `DIRECT_IPC_NVR` / `PLATFORM_GATEWAY` / `ISUP` | Pending | Site |
| Has HikCentral | `yes` / `no` / `unknown` | Pending | Site |
| Has iSecure Center | `yes` / `no` / `unknown` | Pending | Site |
| Has Hikvision OpenAPI | `yes` / `no` / `unknown` | Pending | Site |
| Edge node can reach camera LAN | `yes` / `no` / `unknown` | Pending | Site |
| Public inbound ports allowed for ISUP | `yes` / `no` / `unknown` | Pending | Site |
| Preferred first pilot path | Direct IPC/NVR unless gateway is already ready | Proposed | SP |

Decision rule:

- Choose `DIRECT_IPC_NVR` if the edge node can reach one IPC/NVR and a test
  account is available.
- Choose `PLATFORM_GATEWAY` if HikCentral/iSecure/OpenAPI already manages the
  camera and can provide channel and stream APIs.
- Choose `ISUP` only if devices must register from a private network to our
  server or a Hikvision gateway.

### 7.2 One-Channel Pilot Data

| Field | Example / Placeholder | Status |
| --- | --- | --- |
| siteName | `<site-name>` | Pending |
| accessMode | `<DIRECT_IPC_NVR or PLATFORM_GATEWAY or ISUP>` | Pending |
| endpoint | `<host-or-domain>:<port>` | Pending |
| sdkPort | `<8000 or onsite value>` | Pending |
| rtspPort | `<554 or onsite value>` | Pending |
| httpPort | `<80/443 or onsite value>` | Pending |
| platformBaseUrl | `<https://hik-platform-host:port>` | Pending |
| vendorDeviceCode | `<hik-device-code>` | Pending |
| vendorChannelCode | `<hik-channel-code>` | Pending |
| channelNo | `<channel-number>` | Pending |
| streamType | `main` / `sub` / `third` | Pending |
| streamOutput | `RTSP` / `FLV` / `HLS` / `WebRTC` | Pending |
| edgeNodeId | `<edge-node-id>` | Pending |
| credentialRef | `<secret-ref-name>` | Pending |
| callbackBaseUrl | `<our-callback-url-if-needed>` | Pending |

Implementation may start for a real one-channel video pilot only when
`endpoint`, port, `vendorDeviceCode`, `vendorChannelCode`, `channelNo`,
`streamType`, and `credentialRef` are all confirmed.

### 7.3 Test Account Permission Matrix

| Permission | Required For P0 | Current Status | Notes |
| --- | --- | --- | --- |
| Device login / API authentication | Yes | Pending | Store secret as `credentialRef` only |
| Channel read | Yes | Pending | Needed for sync channels |
| Live preview | Yes | Pending | Needed for video wall/session |
| Snapshot | Yes | Pending | SDK/API snapshot or edge frame capture |
| Recording query | Preferred | Pending | Needed if using device/NVR playback |
| Playback stream | Preferred | Pending | Needed for historical evidence |
| Event subscription | Preferred | Pending | Required for real device alarms |
| PTZ | Optional | Pending | Only for PTZ-capable cameras |
| Resource tree read | Gateway only | Pending | HikCentral/iSecure/OpenAPI |
| Stream URL acquisition | Gateway only | Pending | Gateway stream API |
| Callback whitelist | Event/ISUP only | Pending | Confirm callback URL and firewall |

Blocking permissions for programmer A:

- Channel read.
- Live preview.
- At least one valid secret reference.
- At least one stable channel code.

Snapshot, recording, event, and PTZ can be implemented as explicit
`unsupported` or `unavailable` responses until the onsite account grants those
permissions.

### 7.4 CredentialRef Management

Accepted options:

- `.env` variable on the edge node or backend server.
- Docker secret or mounted secret file.
- Cloud secret manager key name.
- Customer-managed vault key name.

Required naming convention for the first pilot:

```text
credentialRef=hikvision-pilot-001
```

The secret behind this reference must include the actual username and password
or AppKey/AppSecret, but the secret value must never be committed into docs or
code.

### 7.5 Network And Firewall Confirmation

Direct IPC/NVR:

- Edge node to camera/NVR SDK port: required.
- Edge node to camera/NVR RTSP port: required if using RTSP.
- Edge node to camera/NVR HTTP/HTTPS port: optional for ISAPI/snapshot.

Platform gateway:

- Backend or edge node to gateway API base URL: required.
- Backend callback URL reachable from gateway: required only for event push.
- Gateway stream URL reachable by edge node or browser: required.

ISUP:

- Device to ISUP registration endpoint: required.
- Device to stream/alarm service ports: required.
- Public IP/domain, NAT, and security group mapping must be confirmed before
  coding against real ISUP.

### 7.6 Programmer A Start Conditions

Programmer A can start real HIKVISION adapter implementation when all of these
are true:

- Access mode is confirmed.
- One test channel is confirmed.
- `credentialRef` exists in the target runtime environment.
- Required SDK/OpenAPI documentation package is available for the exact access
  mode.
- Network path from edge/backend to camera or gateway is confirmed.
- Live preview permission is confirmed.

If any item is missing, backend should keep returning clear `unavailable`
messages for HIKVISION real access and continue using `MOCK_VENDOR` for QA.

## 8. Open Questions For Project Owner

1. Which path is available onsite first: direct IPC/NVR, HikCentral/iSecure
   gateway, or ISUP?
2. Can onsite provide one non-production test camera or one NVR channel?
3. Can onsite create a least-privilege test account with channel read and live
   preview permission?
4. Should P0 evidence recording use device/NVR recording or platform-side
   edge clip storage?
5. Is event subscription required in P0, or can P0 use polling/manual trigger
   while event access is being approved?

## 9. Onsite Request Form

Copy this section to the onsite contact. The onsite team should fill values with
test-environment information only. Passwords, tokens, AppSecret values, and
private keys must be delivered through the agreed secret channel, not through
this document.

### 9.1 Choose One Access Path

```text
Access path:
[ ] Direct IPC
[ ] NVR
[ ] HikCentral
[ ] iSecure Center
[ ] Hikvision OpenAPI gateway
[ ] ISUP/EHome

Can the edge/backend network reach this device or gateway?
[ ] Yes
[ ] No
[ ] Unknown

Is this a non-production test channel?
[ ] Yes
[ ] No
```

### 9.2 Direct IPC / NVR Parameters

```text
Device type:
Device model:
Firmware version:
Device or NVR host:
SDK port:
RTSP port:
HTTP port:
HTTPS port:
Test account username:
credentialRef name:
Channel number:
Channel name:
Main stream code or path:
Sub stream code or path:
Preferred stream type for P0:
PTZ supported:
PTZ allowed for test account:
```

### 9.3 Platform Gateway Parameters

```text
Platform product:
Platform version:
API base URL:
Authentication method:
AppKey or integration account name:
credentialRef name:
Organization/resource tree API available:
Camera list API available:
Live stream URL API available:
Playback/record API available:
Snapshot API available:
Event subscription API available:
Callback URL whitelist needed:
HTTPS certificate requirement:
```

### 9.4 ISUP / EHome Parameters

```text
ISUP/EHome version:
Device ID:
credentialRef name:
Public registration host/domain:
Registration port:
Stream service port:
Alarm service port:
Picture/callback port:
NAT/security group mapping owner:
Can device reach registration host:
Can platform receive device stream:
```

### 9.5 One Test Channel

```text
vendorDeviceCode:
vendorChannelCode:
channelNo:
channelName:
streamType:
streamOutput:
snapshotSupported:
recordingQuerySupported:
playbackSupported:
eventSubscriptionSupported:
ptzSupported:
```

### 9.6 Permission Confirmation

```text
Channel read:
[ ] Yes
[ ] No

Live preview:
[ ] Yes
[ ] No

Snapshot:
[ ] Yes
[ ] No

Recording query:
[ ] Yes
[ ] No

Playback stream:
[ ] Yes
[ ] No

Event subscription:
[ ] Yes
[ ] No

PTZ:
[ ] Yes
[ ] No
[ ] Not applicable
```

## 10. Current Blocking List

The real HIKVISION single-channel pilot is blocked by these missing onsite
items:

| Blocking Item | Required For | Current Status |
| --- | --- | --- |
| Final access mode | Adapter selection | Missing |
| Gateway availability | Decide direct/gateway path | Missing |
| One reachable endpoint and port | Connection test | Missing |
| `vendorDeviceCode` | Device mapping | Missing |
| `vendorChannelCode` | Channel mapping | Missing |
| `channelNo` | SDK/RTSP stream selection | Missing |
| `streamType` | Main/sub stream selection | Missing |
| `credentialRef` | Secure credential lookup | Missing |
| Channel read permission | Sync channels | Missing |
| Live preview permission | Video session | Missing |

Non-blocking for first video preview, but required for the full P0 real loop:

- Snapshot permission.
- Recording query / playback permission.
- Event subscription permission.
- PTZ permission when the selected camera supports PTZ.
