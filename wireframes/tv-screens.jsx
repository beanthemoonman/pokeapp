/* tv-screens.jsx — Android TV (leanback) screens, at parity with the phone app:
   Browse · Detail · Items (+detail) · Moves (+detail) · Team · Type Matchup.
   D-pad focus is made explicit via the .pdx-focused ring (--fc = accent color).
   The phone's bottom nav is replaced on TV by a persistent left NAV RAIL (icon +
   label, D-pad focusable). Per-screen filter sidebars sit to the right of the rail
   and are collapsible (defaulting to collapsed in production to maximise grid space). */
const { TVFrame, Sprite, TypeBadge, StatBar, Ic, typeColor, typeName, typeText, hexA, dex3 } = window;
const TV_POKE = window.PDX.dexForGen(window.PDX.currentGen);
const TV_CHAR = window.PDX.byDex(6);
const TV_STATS = window.PDX.STATS;
const TV_TEAM = window.PDX.TEAM;
const TV_GEN = window.PDX.genById(window.PDX.currentGen);
const TV_TYPES = window.PDX.typesForGen(window.PDX.currentGen);
const TVFIRE = typeColor('fire');

// dictionary data + accents (mirrors the phone screens)
const TV_ITEMS = window.PDX.itemsForGen(window.PDX.currentGen);
const TV_MOVES = window.PDX.movesForGen(window.PDX.currentGen);
const TV_ITEM_CATS = window.PDX.ITEM_CATEGORIES;
const ITEM_ACCENT = '#C9A24A';                 // neutral gold — items have no type color
const TVDRAGON = typeColor('dragon');          // moves dictionary accent (matches phone)
const TVGRASS = '#62C24A';                      // team accent
const catColor = { physical: '#E0712F', special: '#5C8BD6', status: '#9AA0AC' };
const catLabel = (id) => (TV_ITEM_CATS.find((c) => c.id === id) || { label: id }).label;
const TV_DMG = [
  { id: 'all', label: 'All' },
  { id: 'physical', label: 'Physical' },
  { id: 'special', label: 'Special' },
  { id: 'status', label: 'Status' },
];

// defensive matchup sample (Charizard's fire/flying combo)
const TV_DEF = ['fire', 'flying'];
const TV_DGROUPS = window.PDX.groupDefenseGen(TV_DEF, window.PDX.currentGen);
const DEF_META = [
  { key: 'quad', title: 'Double Weak', mult: '×4', color: '#FF5C5C' },
  { key: 'double', title: 'Weak', mult: '×2', color: '#E0712F' },
  { key: 'neutral', title: 'Neutral', mult: '×1', color: '#9AA0AC' },
  { key: 'half', title: 'Resists', mult: '×½', color: '#62C24A' },
  { key: 'quarter', title: 'Double Resists', mult: '×¼', color: '#3FA98F' },
  { key: 'immune', title: 'Immune', mult: '×0', color: '#7A6BB0' },
];

// ── primary navigation rail (replaces the phone bottom nav) ───
function TVBrand({ size = 30, font = 13 }) {
  return (
    <div style={{ width: size, height: size, borderRadius: 8, flexShrink: 0,
      background: `linear-gradient(150deg, ${TVFIRE}, #D6435A)`,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: font, color: '#15140f' }}>P</div>
  );
}

function TVNavRail({ active = 'dex', focused = null }) {
  const items = [
    { id: 'dex', label: 'Pokédex', icon: Ic.grid },
    { id: 'items', label: 'Items', icon: Ic.bag },
    { id: 'moves', label: 'Moves', icon: Ic.move },
    { id: 'team', label: 'Team', icon: Ic.team },
    { id: 'matchup', label: 'Matchup', icon: Ic.swords },
  ];
  return (
    <div style={{ width: 84, flex: '0 0 84px', height: '100%', background: 'var(--surface)', borderRight: '1px solid var(--line)',
      display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '24px 0', gap: 6, boxSizing: 'border-box' }}>
      <TVBrand />
      <div style={{ height: 18 }} />
      {items.map((it) => {
        const on = it.id === active;
        const isFocus = it.id === focused;
        return (
          <div key={it.id} className={isFocus ? 'pdx-focused' : ''} style={{ ['--fc']: TVFIRE, width: 64, padding: '11px 0', borderRadius: 13,
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6,
            background: on ? hexA('#ffffff', .10) : 'transparent',
            boxShadow: on ? `inset 0 0 0 1.5px ${hexA('#ffffff', .32)}` : 'none',
            color: on ? 'var(--text)' : 'var(--text-faint)' }}>
            <it.icon s={22} />
            <span style={{ fontFamily: 'var(--f-ui)', fontSize: 9.5, fontWeight: on ? 700 : 500, letterSpacing: '.02em' }}>{it.label}</span>
          </div>
        );
      })}
      <div style={{ marginTop: 'auto', fontFamily: 'var(--f-mono)', fontSize: 9, color: 'var(--text-faint)', writingMode: 'vertical-rl', letterSpacing: '.1em' }}>▲▼ NAV</div>
    </div>
  );
}

// generation block — shared by every filter sidebar
function TVGenBlock() {
  return (
    <div>
      <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.16em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 14 }}>Generation</div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 11, padding: '12px 13px', borderRadius: 11,
        background: hexA(TV_GEN.accent, .16), boxShadow: `inset 0 0 0 1.5px ${hexA(TV_GEN.accent, .5)}` }}>
        <div style={{ width: 30, height: 30, borderRadius: 8, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: `linear-gradient(150deg, ${TV_GEN.accent}, ${hexA(TV_GEN.accent, .4)})`,
          fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 13, color: '#15140f' }}>{TV_GEN.label}</div>
        <div style={{ minWidth: 0, flex: 1 }}>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 13.5, fontWeight: 700, color: 'var(--text)' }}>{TV_GEN.region}</div>
          <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10.5, color: 'var(--text-dim)' }}>#1–{TV_GEN.dexEnd}</div>
        </div>
      </div>
      <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10.5, color: 'var(--text-faint)', marginTop: 8, letterSpacing: '.04em' }}>≡ MENU TO CHANGE</div>
    </div>
  );
}

// generic chip-list filter section for a sidebar (type / category / class)
function TVFilterSection({ title, options, activeId, accent }) {
  return (
    <div style={{ minHeight: 0, overflow: 'hidden' }}>
      <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.16em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 14 }}>{title}</div>
      <div className="pdx-scroll" style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
        {options.map((o) => {
          const on = o.id === activeId;
          const c = o.color || accent || 'var(--text)';
          return (
            <div key={o.id} style={{ display: 'flex', alignItems: 'center', gap: 11, padding: '7px 12px', borderRadius: 9,
              background: on ? hexA(typeof c === 'string' && c.startsWith('#') ? c : '#ffffff', .16) : 'transparent',
              boxShadow: on ? `inset 0 0 0 1.5px ${hexA(typeof c === 'string' && c.startsWith('#') ? c : '#ffffff', .5)}` : 'none' }}>
              <span style={{ width: 9, height: 9, borderRadius: 3, background: c }} />
              <span style={{ fontFamily: 'var(--f-ui)', fontSize: 13, fontWeight: on ? 700 : 500, color: on ? 'var(--text)' : 'var(--text-dim)' }}>{o.label}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// the Browse type sidebar (kept from the original wireframe)
function TVSidebar({ activeType = 'all' }) {
  const types = ['all', 'fire', 'water', 'grass', 'electric', 'psychic', 'ghost', 'dragon', 'fighting'];
  const opts = types.map((t) => ({ id: t, label: t === 'all' ? 'All Types' : typeName(t), color: t === 'all' ? '#ffffff' : typeColor(t) }));
  return (
    <div style={{ width: 232, flex: '0 0 232px', height: '100%', background: 'var(--surface)', borderRight: '1px solid var(--line)',
      padding: '28px 20px', display: 'flex', flexDirection: 'column', gap: 26, boxSizing: 'border-box' }}>
      <TVFilterSection title="Type" options={opts} activeId={activeType} />
      <TVGenBlock />
    </div>
  );
}

// footer D-pad hint strip
function TVHints({ hints }) {
  return (
    <div style={{ marginTop: 'auto', paddingTop: 14, display: 'flex', alignItems: 'center', gap: 18, color: 'var(--text-faint)' }}>
      {hints.map((h, i) => (
        <span key={i} style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, letterSpacing: '.04em' }}>{h}</span>
      ))}
    </div>
  );
}

// header search pill (focusable in production)
function TVSearchPill({ placeholder, focused = false, accent = TVFIRE }) {
  return (
    <div className={focused ? 'pdx-focused' : ''} style={{ ['--fc']: accent, display: 'flex', alignItems: 'center', gap: 11, padding: '11px 18px', borderRadius: 12,
      background: 'var(--surface)', border: `1px solid ${focused ? hexA(accent, .5) : 'var(--line)'}`, color: 'var(--text-faint)', minWidth: 260 }}>
      <Ic.search s={18} />
      <span style={{ fontFamily: 'var(--f-ui)', fontSize: 14 }}>{placeholder}</span>
    </div>
  );
}

// content header: title + mono subtitle on the left, search pill on the right
function TVContentHeader({ title, subtitle, search }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 18 }}>
      <div>
        <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 30, letterSpacing: '-.02em', color: 'var(--text)' }}>{title}</h1>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, color: 'var(--text-faint)', marginTop: 4 }}>{subtitle}</div>
      </div>
      {search}
    </div>
  );
}

// generic full-panel error state
function TVError({ accent, title, body }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 18, padding: '0 60px', textAlign: 'center' }}>
      <div style={{ width: 72, height: 72, borderRadius: 18, display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: accent, background: hexA(accent, .12), boxShadow: `inset 0 0 0 1px ${hexA(accent, .35)}` }}>
        <Ic.alert s={34} />
      </div>
      <div>
        <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 21, color: 'var(--text)', marginBottom: 6 }}>{title}</div>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 14, lineHeight: 1.5, color: 'var(--text-dim)', maxWidth: 460 }}>{body}</div>
      </div>
      <button className="pdx-focused" style={{ ['--fc']: accent, marginTop: 4, height: 46, padding: '0 30px', borderRadius: 12, border: 'none', cursor: 'pointer',
        background: accent, color: '#15140f', fontFamily: 'var(--f-ui)', fontWeight: 700, fontSize: 15 }}>Retry</button>
      <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10.5, color: 'var(--text-faint)', letterSpacing: '.04em', marginTop: 2 }}>ERR_NETWORK · 503</div>
    </div>
  );
}

// ── Browse Grid ──────────────────────────────────────────────
function TVCard({ p, focused }) {
  const c = typeColor(p.types[0]);
  return (
    <div className={focused ? 'pdx-focused' : ''} style={{ ['--fc']: c, borderRadius: 14, padding: 13, position: 'relative',
      background: focused ? `linear-gradient(160deg, ${hexA(c, .22)}, var(--surface-2))` : 'var(--surface)',
      border: `1px solid ${focused ? hexA(c, .5) : 'var(--line)'}`,
      transform: focused ? 'translateY(-4px) scale(1.03)' : 'none', transition: 'transform .15s' }}>
      <div style={{ position: 'absolute', top: 12, right: 14, fontFamily: 'var(--f-mono)', fontSize: 11, color: 'var(--text-faint)' }}>{dex3(p.dex)}</div>
      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 11, marginTop: 2 }}>
        <Sprite pokemon={p} size={76} radius={12} label={false} accent={c} />
      </div>
      <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 15, color: 'var(--text)', textAlign: 'center', marginBottom: 9, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{p.name}</div>
      <div style={{ display: 'flex', justifyContent: 'center', gap: 5, flexWrap: 'wrap' }}>
        {p.types.map((t) => <TypeBadge key={t} type={t} size="sm" />)}
      </div>
    </div>
  );
}

function TVSkelCard() {
  return (
    <div style={{ borderRadius: 14, padding: 13, background: 'var(--surface)', border: '1px solid var(--line)' }}>
      <div className="pdx-skel" style={{ width: 76, height: 76, borderRadius: 12, margin: '2px auto 11px' }} />
      <div className="pdx-skel" style={{ width: '70%', height: 13, borderRadius: 4, margin: '0 auto 10px' }} />
      <div className="pdx-skel" style={{ width: 48, height: 18, borderRadius: 999, margin: '0 auto' }} />
    </div>
  );
}

function TVBrowse() {
  return (
    <TVFrame accent={TVFIRE}>
      <div style={{ display: 'flex', height: '100%' }}>
        <TVNavRail active="dex" />
        <TVSidebar activeType="all" />
        <div style={{ flex: 1, padding: '26px 36px 18px', minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <TVContentHeader
            title="Browse"
            subtitle={`${TV_GEN.dexEnd} ENTRIES · NATIONAL THROUGH GEN ${TV_GEN.label}`}
            search={<TVSearchPill placeholder="Search the Pokédex" />}
          />
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: 14 }}>
            {TV_POKE.slice(0, 16).map((p, i) => <TVCard key={p.dex} p={p} focused={i === 7} />)}
            <TVSkelCard /><TVSkelCard />
          </div>
          <TVHints hints={['◄ ► SELECT', '● OPEN', '≡ MENU TO FILTER']} />
        </div>
      </div>
    </TVFrame>
  );
}

function TVBrowseError() {
  return (
    <TVFrame accent={TVFIRE}>
      <div style={{ display: 'flex', height: '100%' }}>
        <TVNavRail active="dex" />
        <TVSidebar activeType="all" />
        <div style={{ flex: 1, padding: '26px 36px 18px', minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <TVContentHeader title="Browse" subtitle={`NATIONAL THROUGH GEN ${TV_GEN.label}`} />
          <TVError accent={TVFIRE} title="Couldn’t load the Pokédex" body="Check your connection and try again. Your saved data is still available offline." />
        </div>
      </div>
    </TVFrame>
  );
}

// ── TV Detail ────────────────────────────────────────────────
function TVTab({ label, focused, active }) {
  return (
    <div className={focused ? 'pdx-focused' : ''} style={{ ['--fc']: TVFIRE, padding: '12px 22px', borderRadius: 11, cursor: 'pointer',
      background: active ? hexA(TVFIRE, .18) : 'var(--surface)',
      border: `1px solid ${active ? hexA(TVFIRE, .5) : 'var(--line)'}` }}>
      <span style={{ fontFamily: 'var(--f-ui)', fontSize: 14.5, fontWeight: active ? 700 : 500, color: active ? 'var(--text)' : 'var(--text-dim)' }}>{label}</span>
    </div>
  );
}

function TVBackBar({ label = 'BACK TO BROWSE' }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 9, color: 'var(--text-faint)', marginBottom: 22 }}>
      <Ic.back s={18} /><span style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5 }}>{label}</span>
    </div>
  );
}

function TVDetail() {
  return (
    <TVFrame accent={TVFIRE}>
      <div style={{ position: 'absolute', inset: 0, background: `radial-gradient(80% 90% at 20% 10%, ${hexA(TVFIRE, .22)}, transparent 60%)` }} />
      <div style={{ position: 'relative', display: 'flex', height: '100%' }}>
        {/* left half */}
        <div style={{ width: '46%', flex: '0 0 46%', padding: '40px 44px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box', borderRight: '1px solid var(--line)' }}>
          <TVBackBar />
          <div style={{ display: 'flex', gap: 26, alignItems: 'center', marginBottom: 26 }}>
            <div style={{ width: 150, height: 150, borderRadius: 20, position: 'relative', overflow: 'hidden', flexShrink: 0,
              background: `radial-gradient(120% 120% at 50% 35%, ${hexA(TVFIRE, .22)}, rgba(255,255,255,.02))`, boxShadow: `inset 0 0 0 1px ${hexA(TVFIRE, .35)}`,
              display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <div style={{ position: 'absolute', inset: 0, background: window.hatch(TVFIRE, .18, .04) }} />
              <span style={{ position: 'relative', fontFamily: 'var(--f-mono)', fontSize: 12, letterSpacing: '.16em', textTransform: 'uppercase', color: hexA(TVFIRE, .92) }}>sprite</span>
            </div>
            <div>
              <div style={{ fontFamily: 'var(--f-mono)', fontSize: 14, color: 'var(--text-faint)' }}>{dex3(TV_CHAR.dex)}</div>
              <h1 style={{ margin: '4px 0 12px', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 42, letterSpacing: '-.02em', color: 'var(--text)' }}>{TV_CHAR.name}</h1>
              <div style={{ display: 'flex', gap: 9 }}>{TV_CHAR.types.map((t) => <TypeBadge key={t} type={t} size="md" />)}</div>
            </div>
          </div>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 16 }}>Base Stats</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 15 }}>
            {TV_STATS.map((s, i) => <StatBar key={s.key} label={s.label} value={TV_CHAR.stats[s.key]} accent={TVFIRE} delay={i * 70} />)}
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 18, paddingTop: 14, borderTop: '1px solid var(--line)' }}>
            <span style={{ fontFamily: 'var(--f-ui)', fontSize: 12, fontWeight: 700, letterSpacing: '.12em', textTransform: 'uppercase', color: 'var(--text-faint)' }}>Base Total</span>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 24, fontWeight: 700, color: 'var(--text)' }}>{TV_CHAR.total}</span>
          </div>
        </div>
        {/* right half */}
        <div style={{ flex: 1, padding: '40px 44px', display: 'flex', flexDirection: 'column', minWidth: 0 }}>
          <div style={{ display: 'flex', gap: 12, marginBottom: 26 }}>
            <TVTab label="Moves" active focused />
            <TVTab label="About" />
            <TVTab label="Evolution" />
          </div>
          <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden' }}>
            {TV_CHAR.moves.slice(0, 7).map((m, i) => (
              <div key={m.name} style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '13px 4px', borderBottom: i < 6 ? '1px solid var(--line)' : 'none' }}>
                <span style={{ flex: 1, fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 16, color: 'var(--text)' }}>{m.name}</span>
                <TypeBadge type={m.type} size="sm" />
                <span style={{ width: 70, textAlign: 'right', fontFamily: 'var(--f-mono)', fontSize: 14, fontWeight: 600, color: 'var(--text)' }}>{m.power || '—'}</span>
                <span style={{ width: 54, textAlign: 'right', fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-faint)' }}>{m.acc}%</span>
              </div>
            ))}
          </div>
          <div style={{ marginTop: 18, display: 'flex', alignItems: 'center', gap: 18, color: 'var(--text-faint)' }}>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5 }}>◄ STATS PANEL</span>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5 }}>► TABS</span>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5 }}>▲▼ ROWS</span>
          </div>
        </div>
      </div>
    </TVFrame>
  );
}

// ── TV Items Dictionary ──────────────────────────────────────
function TVItemTile({ size = 64, radius = 12 }) {
  const c = ITEM_ACCENT;
  return (
    <div style={{ width: size, height: size, borderRadius: radius, position: 'relative', overflow: 'hidden', flexShrink: 0,
      background: `radial-gradient(120% 120% at 50% 30%, ${hexA(c, .16)}, rgba(255,255,255,.02))`,
      boxShadow: `inset 0 0 0 1px ${hexA(c, .28)}`, display: 'flex', alignItems: 'center', justifyContent: 'center', color: hexA(c, .92) }}>
      <div style={{ position: 'absolute', inset: 0, background: window.hatch(c) }} />
      <Ic.bag s={Math.round(size * 0.42)} />
    </div>
  );
}

function TVCost({ cost }) {
  if (!cost) return <span style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, color: 'var(--text-faint)' }}>—</span>;
  return <span style={{ fontFamily: 'var(--f-mono)', fontSize: 13.5, fontWeight: 600, color: 'var(--text)' }}>₽{cost.toLocaleString()}</span>;
}

function TVItemCard({ it, focused }) {
  const c = ITEM_ACCENT;
  return (
    <div className={focused ? 'pdx-focused' : ''} style={{ ['--fc']: c, borderRadius: 14, padding: 14, position: 'relative', display: 'flex', alignItems: 'center', gap: 14,
      background: focused ? `linear-gradient(160deg, ${hexA(c, .22)}, var(--surface-2))` : 'var(--surface)',
      border: `1px solid ${focused ? hexA(c, .5) : 'var(--line)'}`,
      transform: focused ? 'translateY(-3px) scale(1.02)' : 'none', transition: 'transform .15s' }}>
      <TVItemTile />
      <div style={{ minWidth: 0, flex: 1 }}>
        <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 16, color: 'var(--text)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{it.name}</div>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11.5, color: 'var(--text-faint)', marginTop: 3 }}>{catLabel(it.category)}</div>
      </div>
      <TVCost cost={it.cost} />
    </div>
  );
}

function TVItemSkelCard() {
  return (
    <div style={{ borderRadius: 14, padding: 14, background: 'var(--surface)', border: '1px solid var(--line)', display: 'flex', alignItems: 'center', gap: 14 }}>
      <div className="pdx-skel" style={{ width: 64, height: 64, borderRadius: 12 }} />
      <div style={{ flex: 1 }}>
        <div className="pdx-skel" style={{ width: '60%', height: 14, borderRadius: 4, marginBottom: 8 }} />
        <div className="pdx-skel" style={{ width: '35%', height: 10, borderRadius: 4 }} />
      </div>
    </div>
  );
}

function TVItemsSidebar({ active = 'all' }) {
  const opts = TV_ITEM_CATS.map((c) => ({ id: c.id, label: c.label, color: ITEM_ACCENT }));
  return (
    <div style={{ width: 232, flex: '0 0 232px', height: '100%', background: 'var(--surface)', borderRight: '1px solid var(--line)',
      padding: '28px 20px', display: 'flex', flexDirection: 'column', gap: 26, boxSizing: 'border-box' }}>
      <TVFilterSection title="Category" options={opts} activeId={active} accent={ITEM_ACCENT} />
      <TVGenBlock />
    </div>
  );
}

function TVItemsShell({ children, body }) {
  return (
    <TVFrame accent={ITEM_ACCENT}>
      <div style={{ display: 'flex', height: '100%' }}>
        <TVNavRail active="items" />
        <TVItemsSidebar active="all" />
        <div style={{ flex: 1, padding: '26px 36px 18px', minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <TVContentHeader
            title="Items"
            subtitle={`${TV_ITEMS.length} ITEMS · DICTIONARY THROUGH GEN ${TV_GEN.label}`}
            search={<TVSearchPill placeholder="Search items" accent={ITEM_ACCENT} />}
          />
          {body}
          {children}
        </div>
      </div>
    </TVFrame>
  );
}

function TVItems() {
  return (
    <TVItemsShell children={<TVHints hints={['◄ ▲ ▼ ► SELECT', '● OPEN', '≡ MENU TO FILTER']} />}
      body={
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14 }}>
          {TV_ITEMS.slice(0, 9).map((it, i) => <TVItemCard key={it.id} it={it} focused={i === 4} />)}
        </div>
      } />
  );
}

function TVItemsLoading() {
  return (
    <TVItemsShell children={null}
      body={
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14 }}>
          {Array.from({ length: 9 }).map((_, i) => <TVItemSkelCard key={i} />)}
        </div>
      } />
  );
}

function TVItemsError() {
  return (
    <TVFrame accent={ITEM_ACCENT}>
      <div style={{ display: 'flex', height: '100%' }}>
        <TVNavRail active="items" />
        <TVItemsSidebar active="all" />
        <div style={{ flex: 1, padding: '26px 36px 18px', minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <TVContentHeader title="Items" subtitle={`DICTIONARY THROUGH GEN ${TV_GEN.label}`} />
          <TVError accent={ITEM_ACCENT} title="Couldn’t load items" body="Check your connection and try again. Your saved data is still available offline." />
        </div>
      </div>
    </TVFrame>
  );
}

function TVItemDetail({ it = window.PDX.itemById(229) }) {
  const c = ITEM_ACCENT;
  return (
    <TVFrame accent={c}>
      <div style={{ position: 'absolute', inset: 0, background: `radial-gradient(80% 90% at 20% 10%, ${hexA(c, .2)}, transparent 60%)` }} />
      <div style={{ position: 'relative', display: 'flex', height: '100%' }}>
        <div style={{ width: '44%', flex: '0 0 44%', padding: '40px 44px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box', borderRight: '1px solid var(--line)' }}>
          <TVBackBar label="BACK TO ITEMS" />
          <div style={{ display: 'flex', gap: 24, alignItems: 'center', marginBottom: 28 }}>
            <TVItemTile size={130} radius={20} />
            <div style={{ minWidth: 0 }}>
              <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.12em', textTransform: 'uppercase', color: hexA(c, .95) }}>{catLabel(it.category)}</div>
              <h1 style={{ margin: '6px 0 12px', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 34, letterSpacing: '-.02em', color: 'var(--text)' }}>{it.name}</h1>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.08em', textTransform: 'uppercase', color: 'var(--text-faint)' }}>Buy</span>
                <TVCost cost={it.cost} />
              </div>
            </div>
          </div>
          <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', gap: 18, color: 'var(--text-faint)' }}>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5 }}>◄ BACK</span>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5 }}>▲▼ SCROLL</span>
          </div>
        </div>
        <div className="pdx-scroll" style={{ flex: 1, padding: '40px 44px', overflow: 'hidden' }}>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 10 }}>Effect</div>
          <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 16, lineHeight: 1.6, color: 'var(--text)' }}>{it.shortEffect}</p>
          <div style={{ marginTop: 26, padding: '18px 20px', background: 'var(--surface)', borderRadius: 14, border: '1px solid var(--line)' }}>
            <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.12em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 9 }}>Description</div>
            <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 14.5, lineHeight: 1.65, color: 'var(--text-dim)' }}>{it.flavor}</p>
          </div>
        </div>
      </div>
    </TVFrame>
  );
}

// ── TV Moves Dictionary ──────────────────────────────────────
function TVMoveRow({ m, focused }) {
  const c = typeColor(m.type);
  return (
    <div className={focused ? 'pdx-focused' : ''} style={{ ['--fc']: c, display: 'flex', alignItems: 'center', gap: 16, padding: '14px 18px', borderRadius: 12, marginBottom: 8,
      background: focused ? `linear-gradient(160deg, ${hexA(c, .18)}, var(--surface-2))` : 'var(--surface)',
      border: `1px solid ${focused ? hexA(c, .5) : 'var(--line)'}` }}>
      <span style={{ flex: 1, fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 16, color: 'var(--text)' }}>{m.name}</span>
      <TypeBadge type={m.type} size="sm" />
      <span style={{ width: 76, fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.06em', textTransform: 'uppercase', color: catColor[m.cat] }}>{m.cat}</span>
      <span style={{ width: 60, textAlign: 'right', fontFamily: 'var(--f-mono)', fontSize: 15, fontWeight: 600, color: 'var(--text)' }}>{m.power || '—'}</span>
      <span style={{ width: 110, textAlign: 'right', fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-faint)' }}>{m.acc ? m.acc + '%' : '—'} · {m.pp}pp</span>
    </div>
  );
}

function TVMoveSkelRow() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '14px 18px', borderRadius: 12, marginBottom: 8, background: 'var(--surface)', border: '1px solid var(--line)' }}>
      <div className="pdx-skel" style={{ width: '32%', height: 15, borderRadius: 4 }} />
      <div className="pdx-skel" style={{ width: 52, height: 18, borderRadius: 999 }} />
      <div style={{ flex: 1 }} />
      <div className="pdx-skel" style={{ width: 44, height: 14, borderRadius: 4 }} />
      <div className="pdx-skel" style={{ width: 90, height: 11, borderRadius: 4 }} />
    </div>
  );
}

function TVMovesSidebar({ active = 'all' }) {
  const opts = TV_DMG.map((c) => ({ id: c.id, label: c.label, color: c.id === 'all' ? '#ffffff' : catColor[c.id] }));
  return (
    <div style={{ width: 232, flex: '0 0 232px', height: '100%', background: 'var(--surface)', borderRight: '1px solid var(--line)',
      padding: '28px 20px', display: 'flex', flexDirection: 'column', gap: 26, boxSizing: 'border-box' }}>
      <TVFilterSection title="Damage Class" options={opts} activeId={active} />
      <TVGenBlock />
    </div>
  );
}

function TVMovesShell({ children, body }) {
  return (
    <TVFrame accent={TVDRAGON}>
      <div style={{ display: 'flex', height: '100%' }}>
        <TVNavRail active="moves" />
        <TVMovesSidebar active="all" />
        <div style={{ flex: 1, padding: '26px 36px 18px', minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <TVContentHeader
            title="Moves"
            subtitle={`${TV_MOVES.length} MOVES · DICTIONARY THROUGH GEN ${TV_GEN.label}`}
            search={<TVSearchPill placeholder="Search moves" accent={TVDRAGON} />}
          />
          {body}
          {children}
        </div>
      </div>
    </TVFrame>
  );
}

function TVMoves() {
  return (
    <TVMovesShell children={<TVHints hints={['▲▼ ROWS', '● OPEN', '≡ MENU TO FILTER']} />}
      body={
        <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden' }}>
          {TV_MOVES.slice(0, 8).map((m, i) => <TVMoveRow key={m.id} m={m} focused={i === 2} />)}
        </div>
      } />
  );
}

function TVMovesLoading() {
  return (
    <TVMovesShell children={null}
      body={
        <div style={{ flex: 1 }}>
          {Array.from({ length: 8 }).map((_, i) => <TVMoveSkelRow key={i} />)}
        </div>
      } />
  );
}

function TVMovesError() {
  return (
    <TVFrame accent={TVDRAGON}>
      <div style={{ display: 'flex', height: '100%' }}>
        <TVNavRail active="moves" />
        <TVMovesSidebar active="all" />
        <div style={{ flex: 1, padding: '26px 36px 18px', minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <TVContentHeader title="Moves" subtitle={`DICTIONARY THROUGH GEN ${TV_GEN.label}`} />
          <TVError accent={TVDRAGON} title="Couldn’t load moves" body="Check your connection and try again. Your saved data is still available offline." />
        </div>
      </div>
    </TVFrame>
  );
}

function TVMoveStat({ label, value }) {
  return (
    <div style={{ flex: 1, padding: '16px 12px', background: 'var(--surface)', borderRadius: 14, border: '1px solid var(--line)', textAlign: 'center' }}>
      <div style={{ fontFamily: 'var(--f-mono)', fontSize: 26, fontWeight: 700, color: 'var(--text)' }}>{value}</div>
      <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10.5, fontWeight: 700, letterSpacing: '.1em', textTransform: 'uppercase', color: 'var(--text-faint)', marginTop: 6 }}>{label}</div>
    </div>
  );
}

function TVMoveDetail({ m = window.PDX.moveById(53) }) {
  const c = typeColor(m.type);
  return (
    <TVFrame accent={c}>
      <div style={{ position: 'absolute', inset: 0, background: `radial-gradient(80% 90% at 20% 10%, ${hexA(c, .22)}, transparent 60%)` }} />
      <div style={{ position: 'relative', display: 'flex', height: '100%' }}>
        <div style={{ width: '46%', flex: '0 0 46%', padding: '40px 44px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box', borderRight: '1px solid var(--line)' }}>
          <TVBackBar label="BACK TO MOVES" />
          <h1 style={{ margin: '0 0 14px', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 42, letterSpacing: '-.02em', color: 'var(--text)' }}>{m.name}</h1>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 30 }}>
            <TypeBadge type={m.type} size="md" />
            <span style={{ fontFamily: 'var(--f-ui)', fontSize: 12, fontWeight: 700, letterSpacing: '.08em', textTransform: 'uppercase', color: catColor[m.cat] }}>{m.cat}</span>
          </div>
          <div style={{ display: 'flex', gap: 12 }}>
            <TVMoveStat label="Power" value={m.power || '—'} />
            <TVMoveStat label="Acc" value={m.acc ? m.acc + '%' : '—'} />
            <TVMoveStat label="PP" value={m.pp} />
          </div>
          <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', gap: 18, color: 'var(--text-faint)' }}>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5 }}>◄ BACK</span>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5 }}>▲▼ SCROLL</span>
          </div>
        </div>
        <div className="pdx-scroll" style={{ flex: 1, padding: '40px 44px', overflow: 'hidden' }}>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 10 }}>Effect</div>
          <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 16, lineHeight: 1.6, color: 'var(--text)' }}>{m.shortEffect}</p>
        </div>
      </div>
    </TVFrame>
  );
}

// ── TV Team Builder ──────────────────────────────────────────
function TVTeamSlot({ p, focused }) {
  if (!p) {
    return (
      <div className={focused ? 'pdx-focused' : ''} style={{ ['--fc']: TVGRASS, flex: 1, height: '100%', borderRadius: 16,
        border: '1.5px dashed rgba(255,255,255,.18)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 10, color: 'var(--text-faint)' }}>
        <Ic.plus s={26} /><span style={{ fontFamily: 'var(--f-ui)', fontSize: 13, fontWeight: 600 }}>Empty</span>
      </div>
    );
  }
  const c = typeColor(p.types[0]);
  return (
    <div style={{ flex: 1, height: '100%', borderRadius: 16, padding: 16, position: 'relative', overflow: 'hidden',
      background: `linear-gradient(160deg, ${hexA(c, .18)}, var(--surface))`, border: `1px solid ${hexA(c, .32)}`,
      display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 10 }}>
      <div style={{ position: 'absolute', top: 10, right: 12, fontFamily: 'var(--f-mono)', fontSize: 10.5, color: hexA(c, .8) }}>{dex3(p.dex)}</div>
      <Sprite pokemon={p} size={62} radius={12} label={false} accent={c} />
      <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 14, color: 'var(--text)' }}>{p.name}</div>
      <div style={{ display: 'flex', gap: 5 }}>{p.types.map((t) => <TypeBadge key={t} type={t} size="sm" />)}</div>
    </div>
  );
}

const TV_WEAK = { rock: 2, electric: 2, ground: 2, ice: 1, psychic: 1 };
function TVTeam() {
  return (
    <TVFrame accent={TVGRASS}>
      <div style={{ display: 'flex', height: '100%' }}>
        <TVNavRail active="team" />
        <div style={{ flex: 1, padding: '32px 40px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box', minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 22 }}>
            <div>
              <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 28, letterSpacing: '-.02em', color: 'var(--text)' }}>Team Builder</h1>
              <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, color: 'var(--text-faint)', marginTop: 4 }}>5 / 6 SLOTS FILLED</div>
            </div>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 13, color: '#E08A4A' }}>⚠ 5 SHARED WEAKNESSES</span>
          </div>
          {/* top — slots row */}
          <div style={{ display: 'flex', gap: 16, height: 168, marginBottom: 28 }}>
            {TV_TEAM.map((p, i) => <TVTeamSlot key={i} p={p} focused={i === 5} />)}
          </div>
          {/* bottom — coverage */}
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: 24, minHeight: 0 }}>
            <div style={{ background: 'var(--surface)', borderRadius: 16, border: '1px solid var(--line)', padding: '22px 26px' }}>
              <div style={{ fontFamily: 'var(--f-ui)', fontSize: 12, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 18 }}>Defensive Coverage — all {TV_TYPES.length} types</div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(9, 1fr)', gap: 10 }}>
                {TV_TYPES.map((t) => {
                  const w = TV_WEAK[t]; const c = typeColor(t);
                  return (
                    <div key={t} style={{ aspectRatio: '1', borderRadius: 9, position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center',
                      background: w ? c : hexA(c, .14), opacity: w ? 1 : .45,
                      boxShadow: w ? `0 0 0 2px ${hexA('#FF6B5C', .9)}, 0 0 16px ${hexA(c, .55)}` : 'none' }}>
                      <span style={{ fontFamily: 'var(--f-mono)', fontSize: 9.5, fontWeight: 700, color: w ? typeText(t) : hexA(c, .9), textTransform: 'uppercase' }}>{typeName(t).slice(0, 3)}</span>
                      {w > 1 && <span style={{ position: 'absolute', top: -6, right: -6, width: 17, height: 17, borderRadius: 9, background: '#FF6B5C', color: '#1a0f0d', fontFamily: 'var(--f-mono)', fontSize: 10, fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{w}</span>}
                    </div>
                  );
                })}
              </div>
              <div style={{ display: 'flex', gap: 22, marginTop: 20 }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: 8, fontFamily: 'var(--f-ui)', fontSize: 12, color: 'var(--text-dim)' }}><span style={{ width: 12, height: 12, borderRadius: 3, boxShadow: `0 0 0 2px ${hexA('#FF6B5C', .9)}` }} /> Shared weakness (× members)</span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 8, fontFamily: 'var(--f-ui)', fontSize: 12, color: 'var(--text-dim)' }}><span style={{ width: 12, height: 12, borderRadius: 3, background: 'rgba(255,255,255,.16)' }} /> Resisted / covered</span>
              </div>
            </div>
            <div style={{ background: 'var(--surface)', borderRadius: 16, border: '1px solid var(--line)', padding: '22px 26px', display: 'flex', flexDirection: 'column', gap: 18 }}>
              <div style={{ fontFamily: 'var(--f-ui)', fontSize: 12, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)' }}>Offensive Gaps</div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                {['water', 'fire', 'dragon', 'normal'].map((t) => <TypeBadge key={t} type={t} size="md" soft />)}
              </div>
              <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 13, lineHeight: 1.6, color: 'var(--text-dim)' }}>Your team has no move that hits these types super-effectively. Consider a Ground or Rock attacker to close the gap.</p>
              <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', gap: 12, color: 'var(--text-faint)' }}>
                <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5 }}>● ADD TO EMPTY SLOT</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </TVFrame>
  );
}

// ── TV Type Matchup (defensive) ──────────────────────────────
// Pick 1–2 DEFENDING types; see how every attacking type hits that combo.
function TVMatchupSidebar() {
  return (
    <div style={{ width: 232, flex: '0 0 232px', height: '100%', background: 'var(--surface)', borderRight: '1px solid var(--line)',
      padding: '28px 20px', display: 'flex', flexDirection: 'column', gap: 26, boxSizing: 'border-box' }}>
      <div>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.16em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 12 }}>Matchup</div>
        <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 12.5, lineHeight: 1.6, color: 'var(--text-dim)' }}>Pick one or two defending types. The grid shows what every attacking type does to that combo.</p>
      </div>
      <TVGenBlock />
    </div>
  );
}

function TVDefenderHero({ types }) {
  const c = typeColor(types[0]);
  return (
    <div style={{ borderRadius: 16, padding: '26px 16px', textAlign: 'center', position: 'relative', overflow: 'hidden',
      background: `linear-gradient(160deg, ${hexA(c, .3)}, var(--surface))`, border: `1px solid ${hexA(c, .4)}` }}>
      <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10.5, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 12 }}>Defending types</div>
      <div style={{ fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 26, color: 'var(--text)' }}>{types.map(typeName).join(' / ')}</div>
      <div style={{ marginTop: 14, display: 'flex', justifyContent: 'center', gap: 8 }}>
        {types.map((t) => <TypeBadge key={t} type={t} size="md" />)}
      </div>
    </div>
  );
}

function TVMatchup() {
  return (
    <TVFrame accent={typeColor(TV_DEF[0])}>
      <div style={{ display: 'flex', height: '100%' }}>
        <TVNavRail active="matchup" />
        <TVMatchupSidebar />
        <div style={{ flex: 1, padding: '26px 36px 18px', minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <div style={{ marginBottom: 18 }}>
            <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 30, letterSpacing: '-.02em', color: 'var(--text)' }}>Type Matchup</h1>
            <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, color: 'var(--text-faint)', marginTop: 4 }}>DEFENDING · {TV_TYPES.length} ATTACKERS · GEN {TV_GEN.label}</div>
          </div>
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1fr 1.35fr', gap: 26, minHeight: 0 }}>
            {/* left — defender + picker */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 18, minHeight: 0 }}>
              <TVDefenderHero types={TV_DEF} />
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.12em', textTransform: 'uppercase', color: 'var(--text-faint)' }}>Choose defending types (up to 2)</span>
                <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 600, color: 'var(--text-dim)' }}>Clear</span>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8 }}>
                {TV_TYPES.map((t, i) => (
                  <div key={t} className={i === 0 ? 'pdx-focused' : ''} style={{ ['--fc']: typeColor(t), borderRadius: 999 }}>
                    <TypeBadge type={t} size="sm" soft={!TV_DEF.includes(t)} />
                  </div>
                ))}
              </div>
            </div>
            {/* right — grouped results */}
            <div className="pdx-scroll" style={{ overflow: 'hidden', display: 'flex', flexDirection: 'column', gap: 14 }}>
              <div style={{ fontFamily: 'var(--f-ui)', fontSize: 12, fontWeight: 600, color: 'var(--text-dim)' }}>Every attacker · vs {TV_DEF.map(typeName).join(' / ')}</div>
              {DEF_META.map((g) => (
                <div key={g.key}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 9 }}>
                    <span style={{ width: 4, height: 14, borderRadius: 2, background: g.color }} />
                    <span style={{ fontFamily: 'var(--f-ui)', fontSize: 13, fontWeight: 700, color: 'var(--text)' }}>{g.title}</span>
                    <span style={{ fontFamily: 'var(--f-mono)', fontSize: 12, color: g.color, fontWeight: 600 }}>{g.mult}</span>
                    <span style={{ marginLeft: 'auto', fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-faint)' }}>{TV_DGROUPS[g.key].length}</span>
                  </div>
                  {TV_DGROUPS[g.key].length ? (
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 7 }}>
                      {TV_DGROUPS[g.key].map((t) => <TypeBadge key={t} type={t} size="sm" />)}
                    </div>
                  ) : (
                    <span style={{ fontFamily: 'var(--f-ui)', fontSize: 12, color: 'var(--text-faint)', fontStyle: 'italic' }}>None</span>
                  )}
                </div>
              ))}
            </div>
          </div>
          <TVHints hints={['◄ ▲ ▼ ► PICK TYPE', '● TOGGLE', '▲▼ SCROLL RESULTS']} />
        </div>
      </div>
    </TVFrame>
  );
}

Object.assign(window, {
  TVBrowse, TVBrowseError, TVDetail,
  TVItems, TVItemsLoading, TVItemsError, TVItemDetail,
  TVMoves, TVMovesLoading, TVMovesError, TVMoveDetail,
  TVTeam, TVMatchup,
});
