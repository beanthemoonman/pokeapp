/* ──────────────────────────────────────────────────────────────
   components.jsx — shared design-system primitives for both targets.
   These map directly to composables for the core/ui-common module:
     TypeBadge · StatBar · Sprite  (+ icons, device frames, chrome)
   All colors/tokens come from the CSS custom props in index <head>.
   ────────────────────────────────────────────────────────────── */

const { TYPES, STATS, STAT_MAX } = window.PDX;

// ── token helpers ────────────────────────────────────────────
const typeColor = (id) => (TYPES[id] ? TYPES[id].color : '#888');
const typeName  = (id) => (TYPES[id] ? TYPES[id].name : id);
const typeText  = (id) => (TYPES[id] && TYPES[id].dark ? '#15140f' : '#fff');
const dex3 = (n) => '#' + String(n).padStart(3, '0');
// diagonal-hatch placeholder fill, tinted by a type color
const hatch = (hex, a1 = 0.16, a2 = 0.05) =>
  `repeating-linear-gradient(135deg, ${hexA(hex, a1)} 0 8px, ${hexA(hex, a2)} 8px 16px)`;
function hexA(hex, a) {
  const h = hex.replace('#', '');
  const n = parseInt(h.length === 3 ? h.replace(/(.)/g, '$1$1') : h, 16);
  return `rgba(${(n >> 16) & 255}, ${(n >> 8) & 255}, ${n & 255}, ${a})`;
}

// one-time component CSS (shimmer, transitions, focus pulse)
if (!document.getElementById('pdx-comp-css')) {
  const s = document.createElement('style');
  s.id = 'pdx-comp-css';
  s.textContent = `
  @keyframes pdx-shimmer { 0%{background-position:-200% 0} 100%{background-position:200% 0} }
  .pdx-skel{ background:linear-gradient(90deg,
      rgba(255,255,255,.04) 25%, rgba(255,255,255,.09) 37%, rgba(255,255,255,.04) 63%);
      background-size:200% 100%; animation:pdx-shimmer 1.4s ease-in-out infinite; }
  @keyframes pdx-focus { 0%,100%{ box-shadow:0 0 0 3px var(--fc,#fff), 0 0 0 7px color-mix(in srgb,var(--fc,#fff) 35%, transparent), 0 12px 34px rgba(0,0,0,.55);}
      50%{ box-shadow:0 0 0 3px var(--fc,#fff), 0 0 0 9px color-mix(in srgb,var(--fc,#fff) 22%, transparent), 0 12px 34px rgba(0,0,0,.55);} }
  .pdx-focused{ animation:pdx-focus 1.6s ease-in-out infinite; }
  .pdx-scroll::-webkit-scrollbar{ width:0;height:0 }
  .pdx-bar-fill{ transition:width .6s cubic-bezier(.2,.8,.2,1); }
  `;
  document.head.appendChild(s);
}

// ── TypeBadge ────────────────────────────────────────────────
// Pill, type-color fill. size: 'sm' | 'md' | 'lg'.
function TypeBadge({ type, size = 'md', soft = false }) {
  const pad = { sm: '3px 8px', md: '4px 11px', lg: '6px 15px' }[size];
  const fs  = { sm: 10, md: 11.5, lg: 14 }[size];
  const c = typeColor(type);
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      padding: pad, borderRadius: 999, fontSize: fs, lineHeight: 1,
      fontWeight: 700, letterSpacing: '.06em', textTransform: 'uppercase',
      fontFamily: 'var(--f-ui)', whiteSpace: 'nowrap',
      background: soft ? hexA(c, 0.16) : c,
      color: soft ? c : typeText(type),
      boxShadow: soft ? `inset 0 0 0 1px ${hexA(c, 0.4)}` : 'none',
    }}>{typeName(type)}</span>
  );
}

// ── StatBar ──────────────────────────────────────────────────
// label · value · colored horizontal fill. accent = bar color.
function StatBar({ label, value, max = STAT_MAX, accent = '#fff', delay = 0 }) {
  const pct = Math.max(3, Math.min(100, (value / max) * 100));
  return (
    <div style={{ display: 'grid', gridTemplateColumns: '52px 34px 1fr', alignItems: 'center', gap: 10 }}>
      <span style={{ fontFamily: 'var(--f-ui)', fontSize: 10.5, fontWeight: 600, letterSpacing: '.04em', color: 'var(--text-dim)' }}>{label}</span>
      <span style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, fontWeight: 600, color: 'var(--text)', textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>{value}</span>
      <span style={{ position: 'relative', height: 8, background: 'rgba(255,255,255,.07)', borderRadius: 2, overflow: 'hidden' }}>
        <span className="pdx-bar-fill" style={{ position: 'absolute', inset: 0, width: pct + '%', transitionDelay: delay + 'ms',
          background: accent, borderRadius: 2, boxShadow: `0 0 12px ${hexA(accent, .5)}` }} />
      </span>
    </div>
  );
}

// ── Sprite ───────────────────────────────────────────────────
// Square rounded tile · striped placeholder · mono label.
// In production the hatch layer is replaced by the real sprite image.
function Sprite({ pokemon, size = 96, accent, label = true, radius = 8 }) {
  const c = accent || (pokemon && typeColor(pokemon.types[0])) || '#6b7280';
  return (
    <div style={{
      width: size, height: size, borderRadius: radius, position: 'relative',
      background: `radial-gradient(120% 120% at 50% 30%, ${hexA(c, .14)}, rgba(255,255,255,.02))`,
      boxShadow: `inset 0 0 0 1px ${hexA(c, .28)}`, overflow: 'hidden', flexShrink: 0,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <div style={{ position: 'absolute', inset: 0, background: hatch(c) }} />
      {pokemon && (
        <span style={{ position: 'absolute', top: 6, left: 8, fontFamily: 'var(--f-mono)', fontSize: Math.max(9, size * 0.085),
          fontWeight: 600, color: hexA(c, .85), letterSpacing: '.02em' }}>{dex3(pokemon.dex)}</span>
      )}
      <span style={{ position: 'relative', fontFamily: 'var(--f-mono)', fontSize: Math.max(8, size * 0.075),
        color: hexA(c, .92), letterSpacing: '.14em', textTransform: 'uppercase', textAlign: 'center', padding: '0 6px', lineHeight: 1.4 }}>
        {label ? (pokemon ? pokemon.name : 'sprite') : '◇'}
      </span>
    </div>
  );
}

// ── tiny stroke icons (geometric only) ───────────────────────
const Ic = {
  search: (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"><circle cx="8.5" cy="8.5" r="5.5"/><path d="M16 16l-3.2-3.2"/></svg>,
  plus:   (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M10 4v12M4 10h12"/></svg>,
  filter: (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"><path d="M3 6h14M6 10h8M8 14h4"/></svg>,
  back:   (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 4l-5 6 5 6"/></svg>,
  chevR:  (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M8 4l5 6-5 6"/></svg>,
  x:      (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M5 5l10 10M15 5L5 15"/></svg>,
  grid:   (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="1.7"><rect x="3" y="3" width="6" height="6" rx="1"/><rect x="11" y="3" width="6" height="6" rx="1"/><rect x="3" y="11" width="6" height="6" rx="1"/><rect x="11" y="11" width="6" height="6" rx="1"/></svg>,
  team:   (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><circle cx="7" cy="7" r="3"/><path d="M2.5 16c.6-2.6 2.3-4 4.5-4s3.9 1.4 4.5 4"/><path d="M13.5 5.2a3 3 0 0 1 0 5.6M14 16c-.2-1.2-.6-2.3-1.2-3.1"/></svg>,
  swords: (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M3 3l6.5 6.5M14 14l3 3M12 3l-3 3M14 6l3-3M3 14l3 3M9 11l-3 3"/></svg>,
  alert:  (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M10 3l7.5 13H2.5L10 3z"/><path d="M10 8.5v3.5M10 14.3v.2"/></svg>,
  star:   (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill={p.fill||'none'} stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round"><path d="M10 2.5l2.3 4.7 5.2.8-3.8 3.7.9 5.2L10 14.6 5.4 17l.9-5.2L2.5 8l5.2-.8z"/></svg>,
  bag:    (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M4 7h12l-1 9.5a1 1 0 0 1-1 .9H6a1 1 0 0 1-1-.9L4 7z"/><path d="M7 7V5.5A3 3 0 0 1 13 5.5V7"/></svg>,
  move:   (p) => <svg viewBox="0 0 20 20" width={p.s||18} height={p.s||18} fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M10 2.5L12 8l5.5 2L12 12l-2 5.5L8 12l-5.5-2L8 8z"/></svg>,
};

// ── phone chrome ─────────────────────────────────────────────
function StatusBar({ dark = true }) {
  const c = dark ? 'var(--text)' : '#15140f';
  return (
    <div style={{ height: 40, flex: '0 0 40px', display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 20px', color: c, fontFamily: 'var(--f-mono)' }}>
      <span style={{ fontSize: 13, fontWeight: 600, letterSpacing: '.02em' }}>9:41</span>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <svg width="17" height="11" viewBox="0 0 17 11" fill="currentColor"><rect x="0" y="7" width="3" height="4" rx="1"/><rect x="4.5" y="5" width="3" height="6" rx="1"/><rect x="9" y="2.5" width="3" height="8.5" rx="1"/><rect x="13.5" y="0" width="3" height="11" rx="1" opacity=".4"/></svg>
        <svg width="15" height="11" viewBox="0 0 15 11" fill="none" stroke="currentColor" strokeWidth="1.4"><path d="M1 4.2A9 9 0 0 1 14 4.2M3.2 6.6a6 6 0 0 1 8.6 0M5.4 9a3 3 0 0 1 4.2 0" strokeLinecap="round"/></svg>
        <div style={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <div style={{ width: 22, height: 11, borderRadius: 3, border: `1.3px solid ${c}`, opacity: .9, padding: 1.5 }}>
            <div style={{ width: '72%', height: '100%', background: c, borderRadius: 1 }} />
          </div>
        </div>
      </div>
    </div>
  );
}

function BottomNav({ active = 'dex' }) {
  const tabs = [
    { id: 'dex', label: 'Pokédex', icon: Ic.grid },
    { id: 'items', label: 'Items', icon: Ic.bag },
    { id: 'moves', label: 'Moves', icon: Ic.move },
    { id: 'team', label: 'Team', icon: Ic.team },
    { id: 'matchup', label: 'Matchup', icon: Ic.swords },
  ];
  return (
    <div style={{ flex: '0 0 auto', borderTop: '1px solid var(--line)', background: 'var(--surface)',
      padding: '8px 8px calc(8px + env(safe-area-inset-bottom))', display: 'flex' }}>
      {tabs.map((t) => {
        const on = t.id === active;
        return (
          <div key={t.id} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
            color: on ? 'var(--accent, #fff)' : 'var(--text-faint)' }}>
            <t.icon s={20} />
            <span style={{ fontFamily: 'var(--f-ui)', fontSize: 10, fontWeight: on ? 700 : 500, letterSpacing: '.01em' }}>{t.label}</span>
          </div>
        );
      })}
      <div style={{ position: 'absolute', left: '50%', bottom: 7, transform: 'translateX(-50%)', width: 130, height: 4, borderRadius: 2, background: 'rgba(255,255,255,.25)' }} />
    </div>
  );
}

// FAB — floating action button
function Fab({ icon = Ic.filter, accent = 'var(--accent)', label }) {
  return (
    <button style={{ position: 'absolute', right: 18, bottom: 86, zIndex: 5, height: 52,
      padding: label ? '0 20px 0 16px' : 0, width: label ? 'auto' : 52, borderRadius: label ? 26 : 16,
      border: 'none', cursor: 'pointer', background: accent, color: '#15140f',
      display: 'flex', alignItems: 'center', gap: 9, fontFamily: 'var(--f-ui)', fontWeight: 700, fontSize: 14, whiteSpace: 'nowrap',
      boxShadow: '0 8px 24px rgba(0,0,0,.45), 0 0 0 1px rgba(255,255,255,.08) inset' }}>
      {React.createElement(icon, { s: 22 })}{label}
    </button>
  );
}

// PhoneFrame — device bezel; pass exact inner screen size.
function PhoneFrame({ w = 360, h = 760, accent, children }) {
  return (
    <div style={{ padding: 11, background: 'linear-gradient(160deg,#26282e,#101116)', borderRadius: 40,
      boxShadow: '0 1px 0 rgba(255,255,255,.06) inset, 0 24px 60px rgba(0,0,0,.5)' }}>
      <div style={{ width: w, height: h, borderRadius: 30, overflow: 'hidden', position: 'relative',
        background: 'var(--bg)', display: 'flex', flexDirection: 'column',
        ['--accent']: accent || '#fff', isolation: 'isolate' }}>
        {children}
      </div>
    </div>
  );
}

// TVFrame — 16:9 panel bezel for the leanback screens.
function TVFrame({ w = 1280, h = 720, accent, children }) {
  return (
    <div style={{ padding: 12, background: 'linear-gradient(160deg,#1b1c20,#0a0a0d)', borderRadius: 14,
      boxShadow: '0 30px 80px rgba(0,0,0,.55)' }}>
      <div style={{ width: w, height: h, borderRadius: 6, overflow: 'hidden', position: 'relative',
        background: 'var(--bg)', ['--accent']: accent || '#fff', isolation: 'isolate' }}>
        {children}
      </div>
      <div style={{ height: 0, position: 'relative' }}>
        <div style={{ position: 'absolute', right: 16, top: 6, width: 5, height: 5, borderRadius: 5,
          background: accent || '#fff', boxShadow: `0 0 8px ${accent || '#fff'}` }} />
      </div>
    </div>
  );
}

// section eyebrow used across panels
function Eyebrow({ children, color = 'var(--text-faint)' }) {
  return <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.16em',
    textTransform: 'uppercase', color }}>{children}</div>;
}

// ── GenerationCard ───────────────────────────────────────────
// Root version-selector tile (phone list + TV grid). size: 'md' | 'lg'.
// focused → D-pad ring (TV); selected → persistent accent border (phone).
// `gen` is an entry from window.PDX.GENERATIONS.
function GenerationCard({ gen, focused = false, selected = false, size = 'md' }) {
  const c = gen.accent;
  const lg = size === 'lg';
  const lit = focused || selected;
  return (
    <div className={focused ? 'pdx-focused' : ''} style={{ ['--fc']: c,
      display: 'flex', alignItems: 'center', gap: lg ? 16 : 13, width: '100%', boxSizing: 'border-box',
      padding: lg ? 16 : 13, borderRadius: 16, position: 'relative', overflow: 'hidden',
      background: lit ? `linear-gradient(150deg, ${hexA(c, .2)}, var(--surface))` : 'var(--surface)',
      border: `1px solid ${lit ? hexA(c, .5) : 'var(--line)'}`,
      transform: focused ? 'translateY(-3px) scale(1.02)' : 'none', transition: 'transform .15s' }}>
      {/* region crest placeholder — roman numeral on accent */}
      <div style={{ width: lg ? 60 : 50, height: lg ? 60 : 50, borderRadius: 14, flexShrink: 0,
        background: `radial-gradient(120% 120% at 50% 30%, ${c}, ${hexA(c, .35)})`,
        display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: `inset 0 0 0 1px ${hexA(c, .5)}` }}>
        <span style={{ fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: lg ? 20 : 17, color: '#15140f' }}>{gen.label}</span>
      </div>
      <div style={{ minWidth: 0, flex: 1 }}>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10.5, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: hexA(c, .95) }}>Generation {gen.label}</div>
        <div style={{ fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: lg ? 20 : 17, color: 'var(--text)', letterSpacing: '-.01em', margin: '2px 0 4px' }}>{gen.region}</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11, color: 'var(--text-dim)' }}>#1–{gen.dexEnd}</span>
          <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11, color: 'var(--text-faint)' }}>· {gen.dexEnd} entries</span>
        </div>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, color: 'var(--text-faint)', marginTop: 6, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{gen.versions.join(' · ')}</div>
      </div>
      <div style={{ color: lit ? c : 'var(--text-faint)', flexShrink: 0 }}><Ic.chevR s={18} /></div>
    </div>
  );
}

// ── VersionChip ──────────────────────────────────────────────
// Header pill showing the active generation; tap to re-open the root selector.
function VersionChip({ gen }) {
  const c = gen.accent;
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 7, padding: '6px 10px 6px 7px', borderRadius: 999,
      background: hexA(c, .14), border: `1px solid ${hexA(c, .4)}`, cursor: 'pointer' }}>
      <span style={{ width: 17, height: 17, borderRadius: 5, background: `linear-gradient(150deg, ${c}, ${hexA(c, .5)})`,
        display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 9, color: '#15140f' }}>{gen.label}</span>
      <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11.5, fontWeight: 700, color: 'var(--text)', letterSpacing: '.02em' }}>{gen.region}</span>
      <Ic.chevR s={13} />
    </span>
  );
}

Object.assign(window, {
  TypeBadge, StatBar, Sprite, Ic, StatusBar, BottomNav, Fab, PhoneFrame, TVFrame, Eyebrow,
  GenerationCard, VersionChip,
  typeColor, typeName, typeText, dex3, hatch, hexA, Eyebrow,
});
