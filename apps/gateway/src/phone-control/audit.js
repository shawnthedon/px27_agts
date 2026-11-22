/**
 * px27m Audit Engine
 * Immutable audit logs for phone control operations
 */

export class AuditEngine {
  constructor(maxEntries = 1000) {
    this.entries = [];
    this.maxEntries = maxEntries;
  }

  logExecution(entry) {
    const record = {
      audit_id: `aud_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      timestamp_ms: Date.now(),
      command_id: entry.command_id,
      capability: entry.capability,
      agent_id: entry.agent_id,
      autonomy_profile: entry.autonomy_profile,
      platform: entry.platform,
      status: entry.status,
      verified: entry.verified ?? false,
      verification_method: entry.verification_method || null,
      duration_ms: entry.duration_ms || 0,
      error: entry.error || null
    };

    this.entries.unshift(record);
    if (this.entries.length > this.maxEntries) {
      this.entries.pop();
    }
    return record;
  }

  getAuditTrail(filter = {}) {
    return this.entries.filter(e => {
      if (filter.agent_id && e.agent_id !== filter.agent_id) return false;
      if (filter.capability && e.capability !== filter.capability) return false;
      if (filter.status && e.status !== filter.status) return false;
      return true;
    });
  }
}
