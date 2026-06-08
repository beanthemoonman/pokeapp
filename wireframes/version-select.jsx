/* version-select.jsx — ROOT screen: pick a generation before entering the app.
   The selected generation is the global context for every downstream screen:
   it sets the cumulative National Dex (#1..dexEnd) and the era's type chart.
   Phone: scrollable list. TV: D-pad grid. Both precede the app shell. */
const { PhoneFrame, StatusBar, TVFrame, GenerationCard, Eyebrow, Ic, hexA } = window;
const GENS = window.PDX.GENERATIONS;
const VS_ACCENT = window.PDX.genById(1).accent;

// ── Phone — "Choose your Pokédex" ────────────────────────────
function SelectHeader() {
  return (
    <div style={{ padding: '8px 20px 16px', flex: '0 0 auto' }}>
      <Eyebrow color={hexA(VS_ACCENT, .95)}>Pokédex</Eyebrow>
      <h1 style={{ margin: '8px 0 6px', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 27, letterSpacing: '-.02em', color: 'var(--text)' }}>
        Choose your Pokédex
      </h1>
      <p style={{ margin: 0, fontFamily: 'var(--f-ui)', fontSize: 13.5, lineHeight: 1.5, color: 'var(--text-dim)' }}>
        Pick a generation. It sets which Pokémon you’ll browse and the type chart for that era — you can switch any time.
      </p>
    </div>
  );
}

function PhoneVersionSelect() {
  return (
    <PhoneFrame accent={VS_ACCENT}>
      <StatusBar />
      <SelectHeader />
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', padding: '0 16px 16px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        {GENS.map((g) => <GenerationCard key={g.id} gen={g} size="lg" selected={g.id === 1} />)}
      </div>
    </PhoneFrame>
  );
}

// ── Phone — loading skeleton ─────────────────────────────────
function SelectSkelCard() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 16, padding: 16, borderRadius: 16, background: 'var(--surface)', border: '1px solid var(--line)' }}>
      <div className="pdx-skel" style={{ width: 60, height: 60, borderRadius: 14, flexShrink: 0 }} />
      <div style={{ flex: 1 }}>
        <div className="pdx-skel" style={{ width: '34%', height: 10, borderRadius: 4, marginBottom: 9 }} />
        <div className="pdx-skel" style={{ width: '55%', height: 16, borderRadius: 4, marginBottom: 9 }} />
        <div className="pdx-skel" style={{ width: '72%', height: 10, borderRadius: 4 }} />
      </div>
    </div>
  );
}
function PhoneVersionSelectLoading() {
  return (
    <PhoneFrame accent={VS_ACCENT}>
      <StatusBar />
      <SelectHeader />
      <div className="pdx-scroll" style={{ flex: 1, overflow: 'hidden', padding: '0 16px 16px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        {Array.from({ length: 6 }).map((_, i) => <SelectSkelCard key={i} />)}
      </div>
    </PhoneFrame>
  );
}

// ── TV — leanback selector grid ──────────────────────────────
function TVVersionSelect() {
  return (
    <TVFrame accent={VS_ACCENT}>
      <div style={{ position: 'absolute', inset: 0, background: `radial-gradient(70% 80% at 18% 8%, ${hexA(VS_ACCENT, .2)}, transparent 60%)` }} />
      <div style={{ position: 'relative', height: '100%', padding: '40px 48px', display: 'flex', flexDirection: 'column', boxSizing: 'border-box' }}>
        <div style={{ marginBottom: 26 }}>
          <Eyebrow color={hexA(VS_ACCENT, .95)}>Pokédex</Eyebrow>
          <h1 style={{ margin: '8px 0 0', fontFamily: 'var(--f-display)', fontWeight: 700, fontSize: 34, letterSpacing: '-.02em', color: 'var(--text)' }}>Choose your Pokédex</h1>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, flex: 1, alignContent: 'start' }}>
          {GENS.map((g, i) => <GenerationCard key={g.id} gen={g} focused={i === 0} />)}
        </div>
        <div style={{ marginTop: 18, display: 'flex', alignItems: 'center', gap: 18, color: 'var(--text-faint)' }}>
          <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, letterSpacing: '.04em' }}>◄ ▲ ▼ ► SELECT</span>
          <span style={{ fontFamily: 'var(--f-mono)', fontSize: 11.5, letterSpacing: '.04em' }}>● CONFIRM</span>
        </div>
      </div>
    </TVFrame>
  );
}

Object.assign(window, { PhoneVersionSelect, PhoneVersionSelectLoading, TVVersionSelect });
