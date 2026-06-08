/* phone-moves.jsx — Moves Dictionary: list + loading/error + move detail.
   Mirrors phone-list.jsx; row layout matches the Detail "Moves" tab.
   Generation-scoped via the VersionChip (movesForGen, strict). */
const { PhoneFrame, StatusBar, BottomNav, Fab, Ic, TypeBadge, typeColor, hexA, VersionChip } = window;
const PM_GEN = window.PDX.genById(window.PDX.currentGen);
const PM_DATA = window.PDX.movesForGen(window.PDX.currentGen);
// damage-class colors — shared with the Pokémon detail Moves tab.
const catColor = { physical: '#E0712F', special: '#5C8BD6', status: '#9AA0AC' };
const DMG_CLASSES = [
  { id: 'all', label: 'All' },
  { id: 'physical', label: 'Physical' },
  { id: 'special', label: 'Special' },
  { id: 'status', label: 'Status' },
];

function SearchBar({ value = '' }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, height: 44, padding: '0 14px',
      background: 'var(--surface-2)', borderRadius: 12, border: '1px solid var(--line)', color: 'var(--text-faint)' }}>
      <Ic.search s={18} />
      <span style={{ fontFamily: 'var(--f-ui)', fontSize: 14.5, color: value ? 'var(--text)' : 'var(--text-faint)' }}>
        {value || 'Search moves'}
      </span>
    </div>
  );
}

function ClassChips({ active = 'all' }) {
  return (
    <div className="pdx-scroll" style={{ display: 'flex', gap: 8, overflowX: 'auto', padding: '0 16px 2px' }}>
      {DMG_CLASSES.map((c) => {
        const on = c.id === active;
        const col = c.id === 'all' ? 'var(--text)' : catColor[c.id];
        return (
          <span key={c.id} style={{ flex: '0 0 auto', padding: '6px 13px', borderRadius: 999,
            fontFamily: 'var(--f-ui)', fontSize: 12.5, fontWeight: 700, letterSpacing: '.03em',
            color: on ? '#15140f' : 'var(--text-dim)',
            background: on ? col : 'var(--surface-2)',
            border: on ? 'none' : '1px solid var(--line)' }}>
            {c.label}
          </span>
        );
      })}
    </div>
  );
}

function MoveRow({ m }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '11px 16px' }}>
      <div style={{ minWidth: 0, flex: 1 }}>
        <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 16, color: 'var(--text)', letterSpacing: '-.01em' }}>{m.name}</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 5 }}>
          <TypeBadge type={m.type} size="sm" />
          <span style={{ fontFamily: 'var(--f-ui)', fontSize: 10.5, fontWeight: 600, letterSpacing: '.06em',
            textTransform: 'uppercase', color: catColor[m.cat] }}>{m.cat}</span>
        </div>
      </div>
      <div style={{ textAlign: 'right', fontFamily: 'var(--f-mono)' }}>
        <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--text)' }}>{m.power || '—'}</div>
        <div style={{ fontSize: 10, color: 'var(--text-faint)', marginTop: 2 }}>{m.acc ? m.acc + '%' : '—'} · {m.pp}pp</div>
      </div>
    </div>
  );
}

function MovesHeader() {
  return (
    <div style={{ padding: '4px 16px 12px', flex: '0 0 auto' }}>
      <div style={{ marginBottom: 12 }}><VersionChip gen={PM_GEN} /></div>
      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 14 }}>
        <div>
          <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 28, letterSpacing: '-.02em', color: 'var(--text)' }}>Moves</h1>
          <div style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, color: 'var(--text-faint)', marginTop: 2, letterSpacing: '.03em' }}>DICTIONARY · THROUGH GEN {PM_GEN.label}</div>
        </div>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-dim)' }}>
          <span style={{ color: 'var(--text)', fontSize: 18, fontWeight: 600 }}>{PM_DATA.length}</span> moves
        </div>
      </div>
      <div style={{ marginBottom: 12 }}><SearchBar /></div>
    </div>
  );
}

function PhoneMoves() {
  return (
    <PhoneFrame accent={typeColor('dragon')}>
      <StatusBar />
      <MovesHeader />
      <ClassChips />
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', marginTop: 6 }}>
        {PM_DATA.slice(0, 8).map((m, i) => (
          <div key={m.id}>
            <MoveRow m={m} />
            {i < Math.min(PM_DATA.length, 8) - 1 && <div style={{ height: 1, background: 'var(--line)', margin: '0 16px' }} />}
          </div>
        ))}
      </div>
      <Fab icon={Ic.filter} accent={typeColor('dragon')} label="Filter" />
      <BottomNav active="moves" />
    </PhoneFrame>
  );
}

// ── loading skeleton state ───────────────────────────────────
function SkelRow() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '11px 16px' }}>
      <div style={{ flex: 1 }}>
        <div className="pdx-skel" style={{ width: '45%', height: 14, borderRadius: 4, marginBottom: 8 }} />
        <div className="pdx-skel" style={{ width: '32%', height: 14, borderRadius: 999 }} />
      </div>
      <div style={{ textAlign: 'right' }}>
        <div className="pdx-skel" style={{ width: 34, height: 13, borderRadius: 4, marginBottom: 6, marginLeft: 'auto' }} />
        <div className="pdx-skel" style={{ width: 54, height: 9, borderRadius: 4, marginLeft: 'auto' }} />
      </div>
    </div>
  );
}
function PhoneMovesLoading() {
  return (
    <PhoneFrame accent={typeColor('dragon')}>
      <StatusBar />
      <MovesHeader />
      <ClassChips />
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', marginTop: 6 }}>
        {Array.from({ length: 8 }).map((_, i) => (
          <div key={i}>
            <SkelRow />
            {i < 7 && <div style={{ height: 1, background: 'var(--line)', margin: '0 16px' }} />}
          </div>
        ))}
      </div>
      <BottomNav active="moves" />
    </PhoneFrame>
  );
}

// ── error state ──────────────────────────────────────────────
function PhoneMovesError() {
  const c = typeColor('dragon');
  return (
    <PhoneFrame accent={c}>
      <StatusBar />
      <MovesHeader />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 18, padding: '0 40px', textAlign: 'center' }}>
        <div style={{ width: 64, height: 64, borderRadius: 16, display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: c, background: hexA(c, .12), boxShadow: `inset 0 0 0 1px ${hexA(c, .35)}` }}>
          <Ic.alert s={30} />
        </div>
        <div>
          <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 19, color: 'var(--text)', marginBottom: 6 }}>Couldn’t load moves</div>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 13.5, lineHeight: 1.5, color: 'var(--text-dim)' }}>Check your connection and try again. Your saved data is still available offline.</div>
        </div>
        <button style={{ marginTop: 4, height: 44, padding: '0 26px', borderRadius: 12, border: 'none', cursor: 'pointer',
          background: c, color: '#15140f', fontFamily: 'var(--f-ui)', fontWeight: 700, fontSize: 14.5 }}>Retry</button>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10.5, color: 'var(--text-faint)', letterSpacing: '.04em', marginTop: 2 }}>ERR_NETWORK · 503</div>
      </div>
      <BottomNav active="moves" />
    </PhoneFrame>
  );
}

// ── move detail ──────────────────────────────────────────────
function MoveBackBar() {
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

function MoveStat({ label, value }) {
  return (
    <div style={{ flex: 1, padding: '12px 10px', background: 'var(--surface)', borderRadius: 12, border: '1px solid var(--line)', textAlign: 'center' }}>
      <div style={{ fontFamily: 'var(--f-mono)', fontSize: 20, fontWeight: 700, color: 'var(--text)' }}>{value}</div>
      <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10, fontWeight: 700, letterSpacing: '.1em', textTransform: 'uppercase', color: 'var(--text-faint)', marginTop: 4 }}>{label}</div>
    </div>
  );
}

function MoveDetail({ m = window.PDX.moveById(53) }) {
  const c = typeColor(m.type);
  return (
    <PhoneFrame accent={c}>
      <StatusBar />
      <MoveBackBar />
      <div style={{ flex: '0 0 auto', padding: '50px 16px 14px' }}>
        <div style={{ padding: 18, borderRadius: 16,
          background: `linear-gradient(135deg, ${hexA(c, .16)}, var(--surface))`, border: `1px solid ${hexA(c, .25)}` }}>
          <h1 style={{ margin: '0 0 10px', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 26, letterSpacing: '-.02em', color: 'var(--text)' }}>{m.name}</h1>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <TypeBadge type={m.type} size="md" />
            <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.08em',
              textTransform: 'uppercase', color: catColor[m.cat] }}>{m.cat}</span>
          </div>
        </div>
      </div>
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', padding: '0 16px 16px' }}>
        <div style={{ display: 'flex', gap: 10, marginBottom: 18 }}>
          <MoveStat label="Power" value={m.power || '—'} />
          <MoveStat label="Acc" value={m.acc ? m.acc + '%' : '—'} />
          <MoveStat label="PP" value={m.pp} />
        </div>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 8 }}>Effect</div>
        <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 14.5, lineHeight: 1.55, color: 'var(--text)' }}>{m.shortEffect}</p>
      </div>
      <BottomNav active="moves" />
    </PhoneFrame>
  );
}

Object.assign(window, { PhoneMoves, PhoneMovesLoading, PhoneMovesError, MoveDetail });
