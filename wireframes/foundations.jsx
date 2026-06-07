/* foundations.jsx — design-system reference card (handoff to Claude Code).
   Token names mirror the Kotlin Color/dimens they should map to. */
const { TypeBadge, StatBar, Sprite, typeColor, typeName, hexA } = window;
const F_TYPES = window.PDX.TYPES;
const F_POKE = window.PDX.byDex(6);

function Swatch({ name, hex, sub }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
      <div style={{ width: 40, height: 40, borderRadius: 8, background: hex, boxShadow: 'inset 0 0 0 1px rgba(255,255,255,.1)', flexShrink: 0 }} />
      <div style={{ minWidth: 0 }}>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 12.5, fontWeight: 600, color: 'var(--text)' }}>{name}</div>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10.5, color: 'var(--text-faint)' }}>{sub || hex}</div>
      </div>
    </div>
  );
}

function FCol({ title, children }) {
  return (
    <div>
      <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.16em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 16 }}>{title}</div>
      {children}
    </div>
  );
}

function Foundations() {
  return (
    <div style={{ width: 900, height: 660, background: 'var(--bg)', padding: '36px 40px', boxSizing: 'border-box',
      display: 'flex', flexDirection: 'column', fontFamily: 'var(--f-ui)' }}>
      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 28, paddingBottom: 22, borderBottom: '1px solid var(--line)' }}>
        <div>
          <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 30, letterSpacing: '-.02em', color: 'var(--text)' }}>Pokédex — Design System</h1>
          <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-faint)', marginTop: 5 }}>core/ui-common · dark theme · shared across phone + TV</div>
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <TypeBadge type="fire" /><TypeBadge type="water" /><TypeBadge type="grass" />
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.05fr 1.3fr', gap: 40, flex: 1, minHeight: 0 }}>
        {/* left: surfaces + type */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 26 }}>
          <FCol title="Surface tokens">
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px 18px' }}>
              <Swatch name="bg" hex="#0c0d11" />
              <Swatch name="surface" hex="#15171d" />
              <Swatch name="surfaceRaised" hex="#1d2027" />
              <Swatch name="textPrimary" hex="#eef0f4" />
              <Swatch name="textDim" hex="#9aa0ac" />
              <Swatch name="textFaint" hex="#5f6571" />
            </div>
          </FCol>
          <FCol title="Typography">
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 14 }}>
                <span style={{ fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 30, color: 'var(--text)', letterSpacing: '-.02em' }}>Charizard</span>
                <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11, color: 'var(--text-faint)' }}>Space Grotesk · 700</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 14 }}>
                <span style={{ fontFamily: 'var(--f-ui)', fontWeight: 600, fontSize: 16, color: 'var(--text)' }}>Stat labels & UI</span>
                <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11, color: 'var(--text-faint)' }}>Space Grotesk · 500–700</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 14 }}>
                <span style={{ fontFamily: 'var(--f-mono)', fontWeight: 600, fontSize: 16, color: 'var(--text)' }}>#006 · 78 / 84 / 78</span>
                <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11, color: 'var(--text-faint)' }}>JetBrains Mono · numbers</span>
              </div>
            </div>
          </FCol>
        </div>

        {/* right: type palette + components */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 26, minHeight: 0 }}>
          <FCol title="Type color tokens — PokemonType.*">
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: 8 }}>
              {Object.keys(F_TYPES).map((t) => (
                <div key={t} style={{ borderRadius: 8, overflow: 'hidden', background: 'var(--surface)', border: '1px solid var(--line)' }}>
                  <div style={{ height: 26, background: typeColor(t) }} />
                  <div style={{ padding: '5px 7px' }}>
                    <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10, fontWeight: 700, color: 'var(--text)' }}>{typeName(t)}</div>
                    <div style={{ fontFamily: 'var(--f-mono)', fontSize: 8.5, color: 'var(--text-faint)' }}>{typeColor(t)}</div>
                  </div>
                </div>
              ))}
            </div>
          </FCol>
          <FCol title="Components">
            <div style={{ display: 'flex', gap: 28, alignItems: 'flex-start' }}>
              <div>
                <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10, color: 'var(--text-faint)', marginBottom: 10 }}>TypeBadge</div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8, alignItems: 'flex-start' }}>
                  <TypeBadge type="fire" size="lg" /><TypeBadge type="water" size="md" /><TypeBadge type="grass" size="sm" />
                  <TypeBadge type="electric" size="md" soft />
                </div>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10, color: 'var(--text-faint)', marginBottom: 10 }}>StatBar</div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 9 }}>
                  <StatBar label="HP" value={78} accent={typeColor('fire')} />
                  <StatBar label="ATK" value={84} accent={typeColor('fire')} />
                  <StatBar label="SPD" value={100} accent={typeColor('fire')} />
                </div>
              </div>
              <div>
                <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10, color: 'var(--text-faint)', marginBottom: 10 }}>Sprite</div>
                <Sprite pokemon={F_POKE} size={86} radius={12} />
              </div>
            </div>
          </FCol>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { Foundations });
