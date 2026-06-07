/* phone-detail.jsx — Pokémon Detail. Settled on Direction B ("Console Card"):
   dense HUD header, segmented tabs, accent only on data.
   Featured: Charizard (#006, Fire/Flying). */
const { PhoneFrame, StatusBar, TypeBadge, Ic, typeColor, hexA, dex3 } = window;
const CHAR = window.PDX.byDex(6);
const PD_STATS = window.PDX.STATS;
const FIRE = typeColor('fire');

function BackBar() {
  return (
    <div style={{ position: 'absolute', top: 40, left: 0, right: 0, zIndex: 4, height: 44,
      display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 14px' }}>
      {[Ic.back, Ic.star].map((I, i) => (
        <button key={i} style={{ width: 38, height: 38, borderRadius: 11, border: 'none', cursor: 'pointer',
          background: 'rgba(10,10,14,.45)', backdropFilter: 'blur(8px)', color: 'var(--text)',
          display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <I s={19} />
        </button>
      ))}
    </div>
  );
}

function HeroSprite({ size = 92, accent = FIRE }) {
  return (
    <div style={{ width: size, height: size, borderRadius: 14, position: 'relative', overflow: 'hidden', flexShrink: 0,
      background: `radial-gradient(120% 120% at 50% 35%, ${hexA(accent, .2)}, rgba(255,255,255,.02))`,
      boxShadow: `inset 0 0 0 1px ${hexA(accent, .35)}`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div style={{ position: 'absolute', inset: 0, background: window.hatch(accent, .18, .04) }} />
      <span style={{ position: 'relative', fontFamily: 'var(--f-mono)', fontSize: Math.max(9, size * 0.11), letterSpacing: '.16em',
        textTransform: 'uppercase', color: hexA(accent, .92) }}>sprite</span>
    </div>
  );
}

function SegTabs({ active }) {
  const tabs = [['stats', 'Stats'], ['moves', 'Moves'], ['about', 'About'], ['evo', 'Evo']];
  return (
    <div style={{ flex: '0 0 auto', display: 'flex', gap: 4, padding: 4, margin: '0 16px', borderRadius: 12,
      background: 'var(--surface)', border: '1px solid var(--line)' }}>
      {tabs.map(([id, label]) => {
        const on = id === active;
        return (
          <div key={id} style={{ flex: 1, textAlign: 'center', padding: '8px 0', borderRadius: 9, cursor: 'pointer',
            background: on ? hexA(FIRE, .18) : 'transparent',
            boxShadow: on ? `inset 0 0 0 1px ${hexA(FIRE, .5)}` : 'none' }}>
            <span style={{ fontFamily: 'var(--f-ui)', fontSize: 12.5, fontWeight: on ? 700 : 500, color: on ? FIRE : 'var(--text-faint)' }}>{label}</span>
          </div>
        );
      })}
    </div>
  );
}

// Direction B shell — header card + segmented tabs + scroll region
function DetailShellB({ active, children }) {
  return (
    <PhoneFrame accent={FIRE}>
      <StatusBar />
      <BackBar />
      <div style={{ flex: '0 0 auto', padding: '50px 16px 14px' }}>
        <div style={{ display: 'flex', gap: 16, alignItems: 'center', padding: 16, borderRadius: 16,
          background: `linear-gradient(135deg, ${hexA(FIRE, .14)}, var(--surface))`,
          border: `1px solid ${hexA(FIRE, .25)}` }}>
          <HeroSprite size={92} />
          <div style={{ minWidth: 0, flex: 1 }}>
            <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-faint)', letterSpacing: '.06em' }}>{dex3(CHAR.dex)}</div>
            <h1 style={{ margin: '2px 0 8px', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 25, letterSpacing: '-.02em', color: 'var(--text)' }}>{CHAR.name}</h1>
            <div style={{ display: 'flex', gap: 7 }}>
              {CHAR.types.map((t) => <TypeBadge key={t} type={t} size="sm" />)}
            </div>
          </div>
        </div>
      </div>
      <div style={{ padding: '2px 0 14px' }}><SegTabs active={active} /></div>
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden' }}>{children}</div>
    </PhoneFrame>
  );
}

// ── Stats tab — numeric-forward grid ─────────────────────────
function StatPane() {
  return (
    <div style={{ padding: '4px 16px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
        <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)' }}>Base Stats</span>
        <span style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, color: 'var(--text-dim)' }}>Σ <span style={{ color: 'var(--text)', fontSize: 16, fontWeight: 700 }}>{CHAR.total}</span></span>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 13 }}>
        {PD_STATS.map((s, i) => (
          <div key={s.key}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
              <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 600, letterSpacing: '.05em', color: 'var(--text-dim)' }}>{s.full}</span>
              <span style={{ fontFamily: 'var(--f-mono)', fontSize: 13, fontWeight: 700, color: 'var(--text)' }}>{CHAR.stats[s.key]}</span>
            </div>
            <div style={{ height: 6, borderRadius: 1, background: 'rgba(255,255,255,.06)', overflow: 'hidden' }}>
              <div className="pdx-bar-fill" style={{ height: '100%', width: (CHAR.stats[s.key] / 255 * 100) + '%', transitionDelay: i * 60 + 'ms',
                background: `linear-gradient(90deg, ${hexA(FIRE, .6)}, ${FIRE})`, borderRadius: 1 }} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
const DetailB = () => <DetailShellB active="stats"><StatPane /></DetailShellB>;

// ── Moves tab ────────────────────────────────────────────────
const catColor = { physical: '#E0712F', special: '#5C8BD6', status: '#9AA0AC' };
function MovesList() {
  return (
    <div style={{ padding: '2px 0' }}>
      {CHAR.moves.map((m, i) => (
        <div key={m.name}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '11px 18px' }}>
            <div style={{ minWidth: 0, flex: 1 }}>
              <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 15, color: 'var(--text)' }}>{m.name}</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 5 }}>
                <TypeBadge type={m.type} size="sm" />
                <span style={{ fontFamily: 'var(--f-ui)', fontSize: 10.5, fontWeight: 600, letterSpacing: '.06em',
                  textTransform: 'uppercase', color: catColor[m.cat] }}>{m.cat}</span>
              </div>
            </div>
            <div style={{ textAlign: 'right', fontFamily: 'var(--f-mono)' }}>
              <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text)' }}>{m.power || '—'}</div>
              <div style={{ fontSize: 10, color: 'var(--text-faint)', marginTop: 2 }}>{m.acc}% · {m.pp}pp</div>
            </div>
          </div>
          {i < CHAR.moves.length - 1 && <div style={{ height: 1, background: 'var(--line)', margin: '0 18px' }} />}
        </div>
      ))}
    </div>
  );
}
const DetailMoves = () => <DetailShellB active="moves"><MovesList /></DetailShellB>;

// ── About tab ────────────────────────────────────────────────
function AboutBlock({ label, value }) {
  return (
    <div>
      <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10.5, fontWeight: 700, letterSpacing: '.12em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 5 }}>{label}</div>
      <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 15.5, color: 'var(--text)' }}>{value}</div>
    </div>
  );
}
function AboutPane() {
  return (
    <div style={{ padding: '6px 18px' }}>
      <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 14, lineHeight: 1.6, color: 'var(--text-dim)' }}>{CHAR.flavor}</p>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '18px 16px', marginTop: 20 }}>
        <AboutBlock label="Height" value={CHAR.height + ' m'} />
        <AboutBlock label="Weight" value={CHAR.weight + ' kg'} />
        <AboutBlock label="Category" value={CHAR.genus} />
        <AboutBlock label="Abilities" value="Blaze · Solar Power" />
      </div>
      <div style={{ marginTop: 22, padding: '14px 16px', background: 'var(--surface)', borderRadius: 12, border: '1px solid var(--line)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
          <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.1em', textTransform: 'uppercase', color: 'var(--text-faint)' }}>Catch Rate</span>
          <span style={{ fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-dim)' }}>45 · 5.9%</span>
        </div>
        <div style={{ height: 8, borderRadius: 2, background: 'rgba(255,255,255,.07)', overflow: 'hidden' }}>
          <div style={{ width: '18%', height: '100%', background: FIRE, borderRadius: 2 }} />
        </div>
      </div>
    </div>
  );
}
const DetailAbout = () => <DetailShellB active="about"><AboutPane /></DetailShellB>;

// ── Evolution tab ────────────────────────────────────────────
function EvoNode({ stage }) {
  const c = typeColor(stage.types[0]);
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
      <div style={{ width: 84, height: 84, borderRadius: 14, position: 'relative', overflow: 'hidden',
        background: `radial-gradient(120% 120% at 50% 35%, ${hexA(c, .16)}, rgba(255,255,255,.02))`,
        boxShadow: `inset 0 0 0 1px ${hexA(c, .3)}`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ position: 'absolute', inset: 0, background: window.hatch(c) }} />
        <span style={{ position: 'relative', fontFamily: 'var(--f-mono)', fontSize: 9, color: hexA(c, .9), letterSpacing: '.1em' }}>{dex3(stage.dex)}</span>
      </div>
      <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 13.5, color: 'var(--text)' }}>{stage.name}</div>
    </div>
  );
}
function EvoPane() {
  return (
    <div style={{ padding: '12px 18px' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'center', gap: 8 }}>
        {CHAR.evolution.map((st, i) => (
          <React.Fragment key={st.dex}>
            <EvoNode stage={st} />
            {i < CHAR.evolution.length - 1 && (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', paddingTop: 28, color: 'var(--text-faint)' }}>
                <Ic.chevR s={18} />
                <span style={{ fontFamily: 'var(--f-mono)', fontSize: 9.5, color: 'var(--text-dim)', marginTop: 2 }}>{CHAR.evolution[i + 1].cond}</span>
              </div>
            )}
          </React.Fragment>
        ))}
      </div>
      <div style={{ marginTop: 24, padding: '14px 16px', background: 'var(--surface)', borderRadius: 12, border: '1px solid var(--line)',
        fontFamily: 'var(--f-ui)', fontSize: 12.5, lineHeight: 1.5, color: 'var(--text-dim)' }}>
        This line evolves by leveling up. Mega Evolution variants appear as a separate branch when a Mega Stone is held.
      </div>
    </div>
  );
}
const DetailEvolution = () => <DetailShellB active="evo"><EvoPane /></DetailShellB>;

Object.assign(window, { DetailB, DetailMoves, DetailAbout, DetailEvolution });
