/* phone-list.jsx — Pokédex List screen + loading/error states */
const { PhoneFrame, StatusBar, BottomNav, Fab, Sprite, TypeBadge, Ic, typeColor, VersionChip } = window;
const PL_GEN = window.PDX.genById(window.PDX.currentGen);
const PL_DATA = window.PDX.dexForGen(window.PDX.currentGen);

function SearchBar({ value = '' }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, height: 44, padding: '0 14px',
      background: 'var(--surface-2)', borderRadius: 12, border: '1px solid var(--line)', color: 'var(--text-faint)' }}>
      <Ic.search s={18} />
      <span style={{ fontFamily: 'var(--f-ui)', fontSize: 14.5, color: value ? 'var(--text)' : 'var(--text-faint)' }}>
        {value || 'Search name or №'}
      </span>
      <span style={{ marginLeft: 'auto', fontFamily: 'var(--f-mono)', fontSize: 11, color: 'var(--text-faint)' }}>{PL_GEN.dexEnd}</span>
    </div>
  );
}

function TypeChips({ active = 'all' }) {
  const chips = ['all', 'fire', 'water', 'grass', 'electric', 'psychic', 'ghost'];
  return (
    <div className="pdx-scroll" style={{ display: 'flex', gap: 8, overflowX: 'auto', padding: '0 16px 2px' }}>
      {chips.map((c) => {
        const on = c === active;
        const col = c === 'all' ? 'var(--text)' : typeColor(c);
        return (
          <span key={c} style={{ flex: '0 0 auto', padding: '6px 13px', borderRadius: 999,
            fontFamily: 'var(--f-ui)', fontSize: 12.5, fontWeight: 700, letterSpacing: '.03em',
            textTransform: c === 'all' ? 'none' : 'capitalize',
            color: on ? (c === 'all' ? '#15140f' : window.typeText(c)) : 'var(--text-dim)',
            background: on ? col : 'var(--surface-2)',
            border: on ? 'none' : '1px solid var(--line)' }}>
            {c === 'all' ? 'All' : c}
          </span>
        );
      })}
    </div>
  );
}

function ListRow({ p }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '10px 16px' }}>
      <Sprite pokemon={p} size={52} radius={10} label={false} />
      <div style={{ minWidth: 0, flex: 1 }}>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
          <span style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 16.5, color: 'var(--text)', letterSpacing: '-.01em' }}>{p.name}</span>
        </div>
        <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, color: 'var(--text-faint)', letterSpacing: '.04em' }}>{window.dex3(p.dex)}</span>
      </div>
      <div style={{ display: 'flex', gap: 6 }}>
        {p.types.map((t) => <TypeBadge key={t} type={t} size="sm" />)}
      </div>
    </div>
  );
}

function ListHeader() {
  return (
    <div style={{ padding: '4px 16px 12px', flex: '0 0 auto' }}>
      <div style={{ marginBottom: 12 }}><VersionChip gen={PL_GEN} /></div>
      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 14 }}>
        <div>
          <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 28, letterSpacing: '-.02em', color: 'var(--text)' }}>Pokédex</h1>
          <div style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, color: 'var(--text-faint)', marginTop: 2, letterSpacing: '.03em' }}>NATIONAL · THROUGH GEN {PL_GEN.label}</div>
        </div>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-dim)' }}>
          <span style={{ color: 'var(--text)', fontSize: 18, fontWeight: 600 }}>16</span> / {PL_GEN.dexEnd}
        </div>
      </div>
      <div style={{ marginBottom: 12 }}><SearchBar /></div>
    </div>
  );
}

function PhoneList() {
  return (
    <PhoneFrame accent="#FF7A33">
      <StatusBar />
      <ListHeader />
      <TypeChips />
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', marginTop: 6 }}>
        {PL_DATA.slice(0, 8).map((p, i) => (
          <div key={p.dex}>
            <ListRow p={p} />
            {i < 7 && <div style={{ height: 1, background: 'var(--line)', margin: '0 16px' }} />}
          </div>
        ))}
      </div>
      <Fab icon={Ic.filter} accent="#FF7A33" label="Filter" />
      <BottomNav active="dex" />
    </PhoneFrame>
  );
}

// ── loading skeleton state ───────────────────────────────────
function SkelRow() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '10px 16px' }}>
      <div className="pdx-skel" style={{ width: 52, height: 52, borderRadius: 10 }} />
      <div style={{ flex: 1 }}>
        <div className="pdx-skel" style={{ width: '52%', height: 14, borderRadius: 4, marginBottom: 8 }} />
        <div className="pdx-skel" style={{ width: '28%', height: 10, borderRadius: 4 }} />
      </div>
      <div className="pdx-skel" style={{ width: 54, height: 20, borderRadius: 999 }} />
    </div>
  );
}
function PhoneListLoading() {
  return (
    <PhoneFrame accent="#FF7A33">
      <StatusBar />
      <ListHeader />
      <TypeChips />
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', marginTop: 6 }}>
        {Array.from({ length: 8 }).map((_, i) => (
          <div key={i}>
            <SkelRow />
            {i < 7 && <div style={{ height: 1, background: 'var(--line)', margin: '0 16px' }} />}
          </div>
        ))}
      </div>
      <BottomNav active="dex" />
    </PhoneFrame>
  );
}

// ── error state ──────────────────────────────────────────────
function PhoneListError() {
  return (
    <PhoneFrame accent="#FF7A33">
      <StatusBar />
      <ListHeader />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 18, padding: '0 40px', textAlign: 'center' }}>
        <div style={{ width: 64, height: 64, borderRadius: 16, display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: '#FF7A33', background: window.hexA('#FF7A33', .12), boxShadow: `inset 0 0 0 1px ${window.hexA('#FF7A33', .35)}` }}>
          <Ic.alert s={30} />
        </div>
        <div>
          <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 19, color: 'var(--text)', marginBottom: 6 }}>Couldn’t load the Pokédex</div>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 13.5, lineHeight: 1.5, color: 'var(--text-dim)' }}>Check your connection and try again. Your saved data is still available offline.</div>
        </div>
        <button style={{ marginTop: 4, height: 44, padding: '0 26px', borderRadius: 12, border: 'none', cursor: 'pointer',
          background: '#FF7A33', color: '#15140f', fontFamily: 'var(--f-ui)', fontWeight: 700, fontSize: 14.5 }}>Retry</button>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10.5, color: 'var(--text-faint)', letterSpacing: '.04em', marginTop: 2 }}>ERR_NETWORK · 503</div>
      </div>
      <BottomNav active="dex" />
    </PhoneFrame>
  );
}

Object.assign(window, { PhoneList, PhoneListLoading, PhoneListError });
