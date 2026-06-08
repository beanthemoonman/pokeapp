/* app.jsx — assembles every screen onto the design canvas. */
const {
  DesignCanvas, DCSection, DCArtboard, DCPostIt,
  Foundations,
  PhoneVersionSelect, PhoneVersionSelectLoading, TVVersionSelect,
  PhoneList, PhoneListLoading, PhoneListError,
  DetailB, DetailMoves, DetailAbout, DetailEvolution,
  PhoneTeam, PhoneMatchup,
  TVBrowse, TVDetail, TVTeam,
} = window;

// centering wrapper so device bezels float on the canvas grid
function Center({ children, pt }) {
  return (
    <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', paddingTop: pt || 0 }}>
      {children}
    </div>
  );
}

const phoneAB = { width: 446, height: 858, style: { background: 'transparent', boxShadow: 'none' } };
const tvAB    = { width: 1372, height: 808, style: { background: 'transparent', boxShadow: 'none' } };
const foundAB = { width: 940, height: 700, style: { background: 'transparent', boxShadow: 'none' } };

function App() {
  return (
    <DesignCanvas>
      <DCSection id="root-select" title="Root · Version Select" subtitle="Pick a generation first — sets the cumulative National Dex + era type chart for the whole app">
        <DCArtboard id="vs-phone" label="Phone — choose generation" {...phoneAB}><Center><PhoneVersionSelect /></Center></DCArtboard>
        <DCArtboard id="vs-phone-loading" label="Phone — loading (skeleton)" {...phoneAB}><Center><PhoneVersionSelectLoading /></Center></DCArtboard>
        <DCArtboard id="vs-tv" label="TV — choose generation (D-pad)" {...tvAB}><Center><TVVersionSelect /></Center></DCArtboard>
      </DCSection>

      <DCSection id="foundations" title="Foundations" subtitle="Shared tokens & components — core/ui-common">
        <DCArtboard id="tokens" label="Tokens & Components" {...foundAB}>
          <Center><Foundations /></Center>
        </DCArtboard>
      </DCSection>

      <DCSection id="phone-list" title="Phone · Pokédex List" subtitle="Material 3 · NavHost + bottom nav · ViewModel states">
        <DCArtboard id="list" label="List — loaded" {...phoneAB}><Center><PhoneList /></Center></DCArtboard>
        <DCArtboard id="list-loading" label="List — loading (skeleton)" {...phoneAB}><Center><PhoneListLoading /></Center></DCArtboard>
        <DCArtboard id="list-error" label="List — error" {...phoneAB}><Center><PhoneListError /></Center></DCArtboard>
      </DCSection>

      <DCSection id="phone-detail" title="Phone · Pokémon Detail" subtitle="Console Card direction · Stats / Moves / About / Evolution tabs">
        <DCArtboard id="detail-stats" label="Stats tab" {...phoneAB}><Center><DetailB /></Center></DCArtboard>
        <DCArtboard id="detail-moves" label="Moves tab" {...phoneAB}><Center><DetailMoves /></Center></DCArtboard>
        <DCArtboard id="detail-about" label="About tab" {...phoneAB}><Center><DetailAbout /></Center></DCArtboard>
        <DCArtboard id="detail-evo" label="Evolution tab" {...phoneAB}><Center><DetailEvolution /></Center></DCArtboard>
      </DCSection>

      <DCSection id="phone-tools" title="Phone · Team & Matchup" subtitle="Team Builder + Type Matchup Calculator">
        <DCArtboard id="team" label="Team Builder" {...phoneAB}><Center><PhoneTeam /></Center></DCArtboard>
        <DCArtboard id="matchup" label="Type Matchup" {...phoneAB}><Center><PhoneMatchup /></Center></DCArtboard>
      </DCSection>

      <DCSection id="tv" title="TV · Leanback" subtitle="androidx.tv · D-pad focus traversal, no bottom nav">
        <DCArtboard id="tv-browse" label="Browse Grid" {...tvAB}><Center><TVBrowse /></Center></DCArtboard>
        <DCArtboard id="tv-detail" label="Detail" {...tvAB}><Center><TVDetail /></Center></DCArtboard>
        <DCArtboard id="tv-team" label="Team Builder" {...tvAB}><Center><TVTeam /></Center></DCArtboard>
      </DCSection>
    </DesignCanvas>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
