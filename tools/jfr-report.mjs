// Summarises a JFR recording of the NilLoader client: packet counts/bytes and
// CPU hotspots. Called by tools/debug-nil-jfr.ps1.
import fs from 'node:fs';

const [packetFile, cpuFile] = process.argv.slice(2);

function load(file) {
  if (!file || !fs.existsSync(file)) return null;
  let text = fs.readFileSync(file, 'utf8');
  if (text.charCodeAt(0) === 0xfeff) text = text.slice(1);
  if (!text.trim()) return null;
  return JSON.parse(text);
}

const packets = load(packetFile);
if (packets) {
  const byId = new Map();
  for (const event of (packets.recording?.events ?? packets.events ?? [])) {
    const v = event.values ?? {};
    const id = v.packetId ?? v.packetID ?? '?';
    const key = `${event.type} id=${id}`;
    const entry = byId.get(key) ?? { count: 0, bytes: 0 };
    entry.count++;
    entry.bytes += Number(v.bytes ?? 0);
    byId.set(key, entry);
  }
  const rows = [...byId.entries()].sort((a, b) => b[1].bytes - a[1].bytes).slice(0, 20);
  console.log('=== packets (top 20 by bytes) ===');
  for (const [key, value] of rows) {
    console.log(`${key.padEnd(42)} ${String(value.count).padStart(5)} packets  ${(value.bytes / 1024).toFixed(1)} KiB  (${(value.bytes / Math.max(1, value.count)).toFixed(0)} B/packet)`);
  }
}

const cpu = load(cpuFile);
if (cpu) {
  const byMethod = new Map();
  let total = 0;
  for (const event of (cpu.recording?.events ?? cpu.events ?? [])) {
    const frames = event.values?.stackTrace?.frames ?? [];
    total++;
    const top = frames[0];
    if (!top) continue;
    const method = top.method ?? {};
    const name = `${method.type?.name ?? '?'}.${method.name ?? '?'}`;
    byMethod.set(name, (byMethod.get(name) ?? 0) + 1);
  }
  const rows = [...byMethod.entries()].sort((a, b) => b[1] - a[1]).slice(0, 20);
  console.log(`\n=== CPU samples: ${total} ===`);
  for (const [name, count] of rows) {
    console.log(`${name.padEnd(90)} ${count}`);
  }
}
