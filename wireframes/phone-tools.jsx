/* phone-tools.jsx — Team Builder + Type Matchup Calculator */
const { PhoneFrame, StatusBar, BottomNav, Fab, Sprite, TypeBadge, Ic, typeColor, typeName, hexA, dex3 } = window;
const TEAM = window.PDX.TEAM;
const TYPE_IDS = window.PDX.typeIds;

// ── Team Builder ─────────────────────────────────────────────
function TeamSlot({ p }) {
  if (!p) {
    return (
      <div style={{ aspectRatio: '1', borderRadius: 14, border: '1.5px dashed rgba(255,255,255,.16)',
        display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 8, color: 'var(--text-faint)' }}>
        <Ic.plus s={22} />
        <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 600 }}>Add</span>
      </div>
    );
  }
  const c = typeColor(p.types[0]);
  return (
    <div style={{ aspectRatio: '1', borderRadius: 14, padding: 10, position: 'relative', overflow: 'hidden',
      background: `linear-gradient(155deg, ${hexA(c, .16)}, var(--surface))`, border: `1px solid ${hexA(c, .3)}`,
      display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
      <div style={{ position: 'absolute', top: 7, right: 9, fontFamily: 'var(--f-mono)', fontSize: 9.5, color: hexA(c, .8) }}>{dex3(p.dex)}</div>
      <Sprite pokemon={p} size={48} radius={10} label={false} accent={c} />
      <div>
        <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 13.5, color: 'var(--text)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{p.name}</div>
        <div style={{ display: 'flex', gap: 4, marginTop: 5 }}>
          {p.types.map((t) => <span key={t} style={{ width: 18, height: 5, borderRadius: 3, background: typeColor(t) }} />)}
        </div>
      </div>
    </div>
  );
}

// curated coverage result for the sample team
const WEAK = { rock: 2, electric: 2, ground: 2, ice: 1, psychic: 1 };
const OFF_GAP = ['water', 'fire', 'dragon', 'normal'];

function CoverageMatrix() {
  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
        <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)' }}>Defensive Coverage</span>
        <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11, color: '#E08A4A' }}>5 weak points</span>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: 6 }}>
        {TYPE_IDS.map((t) => {
          const w = WEAK[t];
          const c = typeColor(t);
          return (
            <div key={t} style={{ aspectRatio: '1', borderRadius: 8, position: 'relative',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              background: w ? c : hexA(c, .14),
              boxShadow: w ? `0 0 0 2px ${hexA('#FF6B5C', .9)}, 0 0 14px ${hexA(c, .5)}` : 'none',
              opacity: w ? 1 : .5 }}>
              <span style={{ fontFamily: 'var(--f-mono)', fontSize: 9, fontWeight: 700, letterSpacing: '.02em',
                color: w ? window.typeText(t) : hexA(c, .9), textTransform: 'uppercase' }}>{typeName(t).slice(0, 3)}</span>
              {w > 1 && <span style={{ position: 'absolute', top: -5, right: -5, width: 15, height: 15, borderRadius: 8,
                background: '#FF6B5C', color: '#1a0f0d', fontFamily: 'var(--f-mono)', fontSize: 9, fontWeight: 700,
                display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{w}</span>}
            </div>
          );
        })}
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginTop: 14 }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: 6, fontFamily: 'var(--f-ui)', fontSize: 10.5, color: 'var(--text-dim)' }}>
          <span style={{ width: 10, height: 10, borderRadius: 3, boxShadow: `0 0 0 2px ${hexA('#FF6B5C', .9)}`, background: 'transparent' }} /> Shared weakness
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: 6, fontFamily: 'var(--f-ui)', fontSize: 10.5, color: 'var(--text-dim)' }}>
          <span style={{ width: 10, height: 10, borderRadius: 3, background: 'rgba(255,255,255,.16)' }} /> Covered
        </span>
      </div>
      <div style={{ marginTop: 16, paddingTop: 14, borderTop: '1px solid var(--line)' }}>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.12em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 9 }}>Offensive gaps</div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
          {OFF_GAP.map((t) => <TypeBadge key={t} type={t} size="sm" soft />)}
          <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, color: 'var(--text-faint)', alignSelf: 'center', marginLeft: 2 }}>not hit super-effectively</span>
        </div>
      </div>
    </div>
  );
}

function PhoneTeam() {
  const filled = TEAM.filter(Boolean).length;
  return (
    <PhoneFrame accent="#62C24A">
      <StatusBar />
      <div style={{ padding: '4px 16px 14px', flex: '0 0 auto' }}>
        <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
          <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 26, letterSpacing: '-.02em', color: 'var(--text)' }}>Team Builder</h1>
          <span style={{ fontFamily: 'var(--f-mono)', fontSize: 13, color: 'var(--text-dim)' }}>
            <span style={{ color: 'var(--text)', fontSize: 17, fontWeight: 600 }}>{filled}</span> / 6
          </span>
        </div>
      </div>
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', padding: '0 16px' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10, marginBottom: 22 }}>
          {TEAM.map((p, i) => <TeamSlot key={i} p={p} />)}
        </div>
        <CoverageMatrix />
      </div>
      <Fab icon={Ic.plus} accent="#62C24A" label="Add Pokémon" />
      <BottomNav active="team" />
    </PhoneFrame>
  );
}

// ── Type Matchup Calculator ──────────────────────────────────
const ATK = 'fire';
const GROUPS = window.PDX.groupEffectiveness(ATK);
const GROUP_META = [
  { key: 'super',   title: 'Super Effective', mult: '×2',  color: '#62C24A' },
  { key: 'normal',  title: 'Normal',          mult: '×1',  color: '#9AA0AC' },
  { key: 'notvery', title: 'Not Very Effective', mult: '×½', color: '#E0B575' },
  { key: 'immune',  title: 'No Effect',       mult: '×0',  color: '#D6435A' },
];

function TypePicker({ label, selected }) {
  const c = typeColor(selected);
  return (
    <div style={{ flex: 1 }}>
      <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 8, textAlign: 'center' }}>{label}</div>
      <div style={{ borderRadius: 14, padding: '18px 10px', textAlign: 'center', position: 'relative', overflow: 'hidden',
        background: `linear-gradient(160deg, ${hexA(c, .3)}, var(--surface))`, border: `1px solid ${hexA(c, .4)}` }}>
        <div style={{ fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 19, color: 'var(--text)' }}>{typeName(selected)}</div>
        <div style={{ marginTop: 8, display: 'flex', justifyContent: 'center' }}><TypeBadge type={selected} size="sm" /></div>
      </div>
    </div>
  );
}

function PhoneMatchup() {
  return (
    <PhoneFrame accent={typeColor(ATK)}>
      <StatusBar />
      <div style={{ padding: '4px 16px 12px', flex: '0 0 auto' }}>
        <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 26, letterSpacing: '-.02em', color: 'var(--text)' }}>Type Matchup</h1>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, color: 'var(--text-faint)', marginTop: 2 }}>ATTACKING · ALL DEFENDERS</div>
      </div>
      <div style={{ flex: '0 0 auto', padding: '0 16px 14px', display: 'flex', alignItems: 'center', gap: 12 }}>
        <TypePicker label="Attacker" selected="fire" />
        <div style={{ alignSelf: 'flex-end', paddingBottom: 22, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 14, color: 'var(--text-faint)' }}>vs</div>
        <div style={{ flex: 1 }}>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 8, textAlign: 'center' }}>Defender</div>
          <div style={{ borderRadius: 14, padding: '18px 10px', textAlign: 'center', border: '1.5px dashed rgba(255,255,255,.16)', color: 'var(--text-dim)' }}>
            <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 16 }}>All Types</div>
            <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, marginTop: 8, color: 'var(--text-faint)' }}>tap to narrow</div>
          </div>
        </div>
      </div>
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', padding: '2px 16px 16px' }}>
        {GROUP_META.map((g) => (
          <div key={g.key} style={{ marginBottom: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 9 }}>
              <span style={{ width: 4, height: 14, borderRadius: 2, background: g.color }} />
              <span style={{ fontFamily: 'var(--f-ui)', fontSize: 12, fontWeight: 700, color: 'var(--text)' }}>{g.title}</span>
              <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11, color: g.color, fontWeight: 600 }}>{g.mult}</span>
              <span style={{ marginLeft: 'auto', fontFamily: 'var(--f-mono)', fontSize: 11, color: 'var(--text-faint)' }}>{GROUPS[g.key].length}</span>
            </div>
            {GROUPS[g.key].length ? (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                {GROUPS[g.key].map((t) => <TypeBadge key={t} type={t} size="sm" />)}
              </div>
            ) : (
              <span style={{ fontFamily: 'var(--f-ui)', fontSize: 12, color: 'var(--text-faint)', fontStyle: 'italic' }}>None</span>
            )}
          </div>
        ))}
      </div>
      <BottomNav active="matchup" />
    </PhoneFrame>
  );
}

Object.assign(window, { PhoneTeam, PhoneMatchup });
