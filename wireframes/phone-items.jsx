/* phone-items.jsx — Items Dictionary: list + loading/error + item detail.
   Mirrors phone-list.jsx. Searchable list, tappable rows → ItemDetail.
   Generation-scoped via the VersionChip (best-effort — see itemsForGen). */
const { PhoneFrame, StatusBar, BottomNav, Fab, Ic, hexA, VersionChip } = window;
const PI_GEN = window.PDX.genById(window.PDX.currentGen);
const PI_DATA = window.PDX.itemsForGen(window.PDX.currentGen);
const PI_CATS = window.PDX.ITEM_CATEGORIES;
const ITEM_ACCENT = '#C9A24A'; // neutral gold — items have no type color
const catLabel = (id) => (PI_CATS.find((c) => c.id === id) || { label: id }).label;

// ── square icon tile (sprite placeholder, neutral gold) ──────
function ItemIcon({ size = 46, radius = 10 }) {
  const c = ITEM_ACCENT;
  return (
    <div style={{ width: size, height: size, borderRadius: radius, position: 'relative', overflow: 'hidden', flexShrink: 0,
      background: `radial-gradient(120% 120% at 50% 30%, ${hexA(c, .16)}, rgba(255,255,255,.02))`,
      boxShadow: `inset 0 0 0 1px ${hexA(c, .28)}`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div style={{ position: 'absolute', inset: 0, background: window.hatch(c) }} />
      <Ic.bag s={Math.round(size * 0.42)} />
    </div>
  );
}

function CostTag({ cost }) {
  if (!cost) return <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, color: 'var(--text-faint)' }}>—</span>;
  return (
    <span style={{ fontFamily: 'var(--f-mono)', fontSize: 12.5, fontWeight: 600, color: 'var(--text)', fontVariantNumeric: 'tabular-nums' }}>
      ₽{cost.toLocaleString()}
    </span>
  );
}

function SearchBar({ value = '' }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, height: 44, padding: '0 14px',
      background: 'var(--surface-2)', borderRadius: 12, border: '1px solid var(--line)', color: 'var(--text-faint)' }}>
      <Ic.search s={18} />
      <span style={{ fontFamily: 'var(--f-ui)', fontSize: 14.5, color: value ? 'var(--text)' : 'var(--text-faint)' }}>
        {value || 'Search items'}
      </span>
    </div>
  );
}

function CatChips({ active = 'all' }) {
  return (
    <div className="pdx-scroll" style={{ display: 'flex', gap: 8, overflowX: 'auto', padding: '0 16px 2px' }}>
      {PI_CATS.map((c) => {
        const on = c.id === active;
        return (
          <span key={c.id} style={{ flex: '0 0 auto', padding: '6px 13px', borderRadius: 999,
            fontFamily: 'var(--f-ui)', fontSize: 12.5, fontWeight: 700, letterSpacing: '.03em',
            color: on ? '#15140f' : 'var(--text-dim)',
            background: on ? ITEM_ACCENT : 'var(--surface-2)',
            border: on ? 'none' : '1px solid var(--line)' }}>
            {c.label}
          </span>
        );
      })}
    </div>
  );
}

function ItemRow({ it }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '10px 16px' }}>
      <ItemIcon />
      <div style={{ minWidth: 0, flex: 1 }}>
        <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 16.5, color: 'var(--text)', letterSpacing: '-.01em' }}>{it.name}</div>
        <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11.5, color: 'var(--text-faint)', letterSpacing: '.02em' }}>{catLabel(it.category)}</span>
      </div>
      <CostTag cost={it.cost} />
    </div>
  );
}

function ItemsHeader() {
  return (
    <div style={{ padding: '4px 16px 12px', flex: '0 0 auto' }}>
      <div style={{ marginBottom: 12 }}><VersionChip gen={PI_GEN} /></div>
      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 14 }}>
        <div>
          <h1 style={{ margin: 0, fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 28, letterSpacing: '-.02em', color: 'var(--text)' }}>Items</h1>
          <div style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, color: 'var(--text-faint)', marginTop: 2, letterSpacing: '.03em' }}>DICTIONARY · THROUGH GEN {PI_GEN.label}</div>
        </div>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 12, color: 'var(--text-dim)' }}>
          <span style={{ color: 'var(--text)', fontSize: 18, fontWeight: 600 }}>{PI_DATA.length}</span> items
        </div>
      </div>
      <div style={{ marginBottom: 12 }}><SearchBar /></div>
    </div>
  );
}

function PhoneItems() {
  return (
    <PhoneFrame accent={ITEM_ACCENT}>
      <StatusBar />
      <ItemsHeader />
      <CatChips />
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', marginTop: 6 }}>
        {PI_DATA.slice(0, 8).map((it, i) => (
          <div key={it.id}>
            <ItemRow it={it} />
            {i < Math.min(PI_DATA.length, 8) - 1 && <div style={{ height: 1, background: 'var(--line)', margin: '0 16px' }} />}
          </div>
        ))}
      </div>
      <Fab icon={Ic.filter} accent={ITEM_ACCENT} label="Filter" />
      <BottomNav active="items" />
    </PhoneFrame>
  );
}

// ── loading skeleton state ───────────────────────────────────
function SkelRow() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '10px 16px' }}>
      <div className="pdx-skel" style={{ width: 46, height: 46, borderRadius: 10 }} />
      <div style={{ flex: 1 }}>
        <div className="pdx-skel" style={{ width: '50%', height: 14, borderRadius: 4, marginBottom: 8 }} />
        <div className="pdx-skel" style={{ width: '30%', height: 10, borderRadius: 4 }} />
      </div>
      <div className="pdx-skel" style={{ width: 48, height: 14, borderRadius: 4 }} />
    </div>
  );
}
function PhoneItemsLoading() {
  return (
    <PhoneFrame accent={ITEM_ACCENT}>
      <StatusBar />
      <ItemsHeader />
      <CatChips />
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', marginTop: 6 }}>
        {Array.from({ length: 8 }).map((_, i) => (
          <div key={i}>
            <SkelRow />
            {i < 7 && <div style={{ height: 1, background: 'var(--line)', margin: '0 16px' }} />}
          </div>
        ))}
      </div>
      <BottomNav active="items" />
    </PhoneFrame>
  );
}

// ── error state ──────────────────────────────────────────────
function PhoneItemsError() {
  return (
    <PhoneFrame accent={ITEM_ACCENT}>
      <StatusBar />
      <ItemsHeader />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 18, padding: '0 40px', textAlign: 'center' }}>
        <div style={{ width: 64, height: 64, borderRadius: 16, display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: ITEM_ACCENT, background: hexA(ITEM_ACCENT, .12), boxShadow: `inset 0 0 0 1px ${hexA(ITEM_ACCENT, .35)}` }}>
          <Ic.alert s={30} />
        </div>
        <div>
          <div style={{ fontFamily: 'var(--f-display)', fontWeight: 600, fontSize: 19, color: 'var(--text)', marginBottom: 6 }}>Couldn’t load items</div>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 13.5, lineHeight: 1.5, color: 'var(--text-dim)' }}>Check your connection and try again. Your saved data is still available offline.</div>
        </div>
        <button style={{ marginTop: 4, height: 44, padding: '0 26px', borderRadius: 12, border: 'none', cursor: 'pointer',
          background: ITEM_ACCENT, color: '#15140f', fontFamily: 'var(--f-ui)', fontWeight: 700, fontSize: 14.5 }}>Retry</button>
        <div style={{ fontFamily: 'var(--f-mono)', fontSize: 10.5, color: 'var(--text-faint)', letterSpacing: '.04em', marginTop: 2 }}>ERR_NETWORK · 503</div>
      </div>
      <BottomNav active="items" />
    </PhoneFrame>
  );
}

// ── item detail ──────────────────────────────────────────────
// Reuses the phone-detail back-bar + header-card pattern.
function ItemBackBar() {
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

function ItemDetail({ it = window.PDX.itemById(229) }) {
  const c = ITEM_ACCENT;
  return (
    <PhoneFrame accent={c}>
      <StatusBar />
      <ItemBackBar />
      <div style={{ flex: '0 0 auto', padding: '50px 16px 14px' }}>
        <div style={{ display: 'flex', gap: 16, alignItems: 'center', padding: 16, borderRadius: 16,
          background: `linear-gradient(135deg, ${hexA(c, .14)}, var(--surface))`, border: `1px solid ${hexA(c, .25)}` }}>
          <ItemIcon size={84} radius={14} />
          <div style={{ minWidth: 0, flex: 1 }}>
            <div style={{ fontFamily: 'var(--f-ui)', fontSize: 10.5, fontWeight: 700, letterSpacing: '.12em', textTransform: 'uppercase', color: hexA(c, .95) }}>{catLabel(it.category)}</div>
            <h1 style={{ margin: '3px 0 8px', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 24, letterSpacing: '-.02em', color: 'var(--text)' }}>{it.name}</h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.06em', textTransform: 'uppercase', color: 'var(--text-faint)' }}>Buy</span>
              <CostTag cost={it.cost} />
            </div>
          </div>
        </div>
      </div>
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', padding: '4px 16px 16px' }}>
        <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 8 }}>Effect</div>
        <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 14.5, lineHeight: 1.55, color: 'var(--text)' }}>{it.shortEffect}</p>
        <div style={{ marginTop: 22, padding: '14px 16px', background: 'var(--surface)', borderRadius: 12, border: '1px solid var(--line)' }}>
          <div style={{ fontFamily: 'var(--f-ui)', fontSize: 11, fontWeight: 700, letterSpacing: '.12em', textTransform: 'uppercase', color: 'var(--text-faint)', marginBottom: 8 }}>Description</div>
          <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 13.5, lineHeight: 1.6, color: 'var(--text-dim)' }}>{it.flavor}</p>
        </div>
      </div>
      <BottomNav active="items" />
    </PhoneFrame>
  );
}

Object.assign(window, { PhoneItems, PhoneItemsLoading, PhoneItemsError, ItemDetail });
