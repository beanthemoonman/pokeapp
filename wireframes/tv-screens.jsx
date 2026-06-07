/* tv-screens.jsx — Android TV (leanback) Browse, Detail, Team Builder.
   D-pad focus made explicit via the .pdx-focused ring (--fc = type color). */
const { TVFrame, Sprite, TypeBadge, StatBar, Ic, typeColor, typeName, typeText, hexA, dex3 } = window;
const TV_POKE = window.PDX.POKEMON;
const TV_CHAR = window.PDX.byDex(6);
const TV_STATS = window.PDX.STATS;
const TV_TEAM = window.PDX.TEAM;
const TVFIRE = typeColor('fire');

// ── shared sidebar ───────────────────────────────────────────
function TVSidebar({ activeType = 'all' }) {
  const types = ['all', 'fire', 'water', 'grass', 'electric', 'psychic', 'ghost', 'dragon', 'fighting'];
  const gens = ['Gen I', 'Gen II', 'Gen III', 'Gen IV'];
  return (
    <div style={{ width: 248, flex: '0 0 248px', height: '100%', background: 'var(--surface)', borderRight: '1px solid var(--line)',
      padding: '30px 22px', display: 'flex', flexDirection: 'column', gap: 30, boxSizing: 'border-box' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 11 }}>
        <div style={{ width: 30, height: 30, borderRadius: 8, background: `linear-gradient(150deg, ${TVFIRE}, #D6435A)` }} />
        <span style={{ fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 19, letterSpacing: '-.02em', color: 'var(--text)' }}>Pokédex</span>
      </div>
      <div>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.16em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 14 }}>Type</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {types.map((t) => {
            const on = t === activeType;
            const c = t === 'all' ? 'var(--text)' : typeColor(t);
            return (
              <div key={t} style={{ display: 'flex', alignItems: 'center', gap: 11, padding: '8px 12px', borderRadius: 9,
                background: on ? hexA(t === 'all' ? '#ffffff' : typeColor(t), .16) : 'transparent',
                boxShadow: on ? `inset 0 0 0 1.5px ${hexA(t === 'all' ? '#ffffff' : typeColor(t), .5)}` : 'none' }}>
                <span style={{ width: 9, height: 9, borderRadius: 3, background: c }} />
                <span style={{ fontFamily: 'var(--f-ui)', fontSize: 13.5, fontWeight: on ? 700 : 500,
                  textTransform: t === 'all' ? 'none' : 'capitalize', color: on ? 'var(--text)' : 'var(--text-dim)' }}>{t === 'all' ? 'All Types' : t}</span>
              </div>
            );
          })}
        </div>
      </div>
      <div>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.16em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 14 }}>Generation</div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
          {gens.map((g, i) => (
            <span key={g} style={{ padding: '7px 13px', borderRadius: 8, fontFamily: 'var(--f-ui)', fontSize: 12.5, fontWeight: 600,
              background: i === 0 ? hexA(TVFIRE, .16) : 'var(--surface-2)', color: i === 0 ? TVFIRE : 'var(--text-dim)',
              boxShadow: i === 0 ? `inset 0 0 0 1.5px ${hexA(TVFIRE, .5)}` : 'none' }}>{g}</span>
          ))}
        </div>
      </div>
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
        <TVSidebar activeType="all" />
        <div style={{ flex: 1, padding: '26px 40px 18px', minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 18 }}>
            <div>
              <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 30, letterSpacing: '-.02em', color: 'var(--text)' }}>Browse</h1>
              <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, color: 'var(--text-faint)', marginTop: 4 }}>151 ENTRIES · KANTO</div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 11, padding: '11px 18px', borderRadius: 12,
              background: 'var(--surface)', border: '1px solid var(--line)', color: 'var(--text-faint)', minWidth: 260 }}>
              <Ic.search s={18} />
              <span style={{ fontFamily: 'var(--f-ui)', fontSize: 14 }}>Search the Pokédex</span>
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: 14 }}>
            {TV_POKE.slice(0, 16).map((p, i) => <TVCard key={p.dex} p={p} focused={i === 7} />)}
            <TVSkelCard /><TVSkelCard />
          </div>
          <div style={{ marginTop: 'auto', paddingTop: 14, display: 'flex', alignItems: 'center', gap: 18, color: 'var(--text-faint)' }}>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, letterSpacing: '.04em' }}>◄ ► SELECT</span>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, letterSpacing: '.04em' }}>● OPEN</span>
            <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, letterSpacing: '.04em' }}>≡ MENU TO FILTER</span>
          </div>
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

function TVDetail() {
  return (
    <TVFrame accent={TVFIRE}>
      <div style={{ position: 'absolute', inset: 0, background: `radial-gradient(80% 90% at 20% 10%, ${hexA(TVFIRE, .22)}, transparent 60%)` }} />
      <div style={{ position: 'relative', display: 'flex', height: '100%' }}>
        {/* left half */}
        <div style={{ width: '46%', flex: '0 0 46%', padding: '40px 44px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box', borderRight: '1px solid var(--line)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 9, color: 'var(--text-faint)', marginBottom: 22 }}>
            <Ic.back s={18} /><span style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5 }}>BACK TO BROWSE</span>
          </div>
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

// ── TV Team Builder ──────────────────────────────────────────
function TVTeamSlot({ p, focused }) {
  if (!p) {
    return (
      <div className={focused ? 'pdx-focused' : ''} style={{ ['--fc']: '#62C24A', flex: 1, height: '100%', borderRadius: 16,
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
    <TVFrame accent="#62C24A">
      <div style={{ height: '100%', padding: '32px 40px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box' }}>
        <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 22 }}>
          <div>
            <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 28, letterSpacing: '-.02em', color: 'var(--text)' }}>Team Builder</h1>
            <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, color: 'var(--text-faint)', marginTop: 4 }}>5 / 6 SLOTS FILLED</div>
          </div>
          <span style={{ fontFamily: 'var(--f-mono)', fontSize: 13, color: '#E08A4A' }}>⚠ 5 SHARED WEAKNESSES</span>
        </div>
        {/* top third — slots row */}
        <div style={{ display: 'flex', gap: 16, height: 168, marginBottom: 28 }}>
          {TV_TEAM.map((p, i) => <TVTeamSlot key={i} p={p} focused={i === 5} />)}
        </div>
        {/* bottom — coverage */}
        <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: 24, minHeight: 0 }}>
          <div style={{ background: 'var(--surface)', borderRadius: 16, border: '1px solid var(--line)', padding: '22px 26px' }}>
            <div style={{ fontFamily: 'var(--f-ui)', fontSize: 12, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 18 }}>Defensive Coverage — all 18 types</div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(9, 1fr)', gap: 10 }}>
              {window.PDX.typeIds.map((t) => {
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
    </TVFrame>
  );
}

Object.assign(window, { TVBrowse, TVDetail, TVTeam });
