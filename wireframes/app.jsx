/* app.jsx — assembles every screen onto the design canvas. */
const {
  DesignCanvas, DCSection, DCArtboard, DCPostIt,
  Foundations,
  PhoneVersionSelect, PhoneVersionSelectLoading, TVVersionSelect,
  PhoneList, PhoneListLoading, PhoneListError,
  DetailB, DetailMoves, DetailAbout, DetailEvolution,
  PhoneItems, PhoneItemsLoading, PhoneItemsError, ItemDetail,
  PhoneMoves, PhoneMovesLoading, PhoneMovesError, MoveDetail,
  PhoneTeam, PhoneMatchup,
  TVBrowse, TVBrowseError, TVDetail,
  TVItems, TVItemsLoading, TVItemsError, TVItemDetail,
  TVMoves, TVMovesLoading, TVMovesError, TVMoveDetail,
  TVTeam, TVMatchup,
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

      <DCSection id="phone-items" title="Phone · Items Dictionary" subtitle="Searchable item list · generation-scoped · tap a row for detail">
        <DCArtboard id="items" label="Items — loaded" {...phoneAB}><Center><PhoneItems /></Center></DCArtboard>
        <DCArtboard id="items-loading" label="Items — loading (skeleton)" {...phoneAB}><Center><PhoneItemsLoading /></Center></DCArtboard>
        <DCArtboard id="items-error" label="Items — error" {...phoneAB}><Center><PhoneItemsError /></Center></DCArtboard>
        <DCArtboard id="item-detail" label="Item — detail" {...phoneAB}><Center><ItemDetail /></Center></DCArtboard>
      </DCSection>

      <DCSection id="phone-moves" title="Phone · Moves Dictionary" subtitle="Searchable move list · generation-scoped · tap a row for detail">
        <DCArtboard id="moves" label="Moves — loaded" {...phoneAB}><Center><PhoneMoves /></Center></DCArtboard>
        <DCArtboard id="moves-loading" label="Moves — loading (skeleton)" {...phoneAB}><Center><PhoneMovesLoading /></Center></DCArtboard>
        <DCArtboard id="moves-error" label="Moves — error" {...phoneAB}><Center><PhoneMovesError /></Center></DCArtboard>
        <DCArtboard id="move-detail" label="Move — detail" {...phoneAB}><Center><MoveDetail /></Center></DCArtboard>
      </DCSection>

      <DCSection id="phone-tools" title="Phone · Team & Matchup" subtitle="Team Builder + Type Matchup Calculator">
        <DCArtboard id="team" label="Team Builder" {...phoneAB}><Center><PhoneTeam /></Center></DCArtboard>
        <DCArtboard id="matchup" label="Type Matchup" {...phoneAB}><Center><PhoneMatchup /></Center></DCArtboard>
      </DCSection>

      <DCSection id="tv" title="TV · Leanback" subtitle="androidx.tv · D-pad focus traversal · left nav rail replaces the phone bottom nav">
        <DCArtboard id="tv-browse" label="Browse Grid" {...tvAB}><Center><TVBrowse /></Center></DCArtboard>
        <DCArtboard id="tv-browse-error" label="Browse — error" {...tvAB}><Center><TVBrowseError /></Center></DCArtboard>
        <DCArtboard id="tv-detail" label="Pokémon Detail" {...tvAB}><Center><TVDetail /></Center></DCArtboard>
        <DCArtboard id="tv-items" label="Items — loaded" {...tvAB}><Center><TVItems /></Center></DCArtboard>
        <DCArtboard id="tv-items-loading" label="Items — loading" {...tvAB}><Center><TVItemsLoading /></Center></DCArtboard>
        <DCArtboard id="tv-items-error" label="Items — error" {...tvAB}><Center><TVItemsError /></Center></DCArtboard>
        <DCArtboard id="tv-item-detail" label="Item — detail" {...tvAB}><Center><TVItemDetail /></Center></DCArtboard>
        <DCArtboard id="tv-moves" label="Moves — loaded" {...tvAB}><Center><TVMoves /></Center></DCArtboard>
        <DCArtboard id="tv-moves-loading" label="Moves — loading" {...tvAB}><Center><TVMovesLoading /></Center></DCArtboard>
        <DCArtboard id="tv-moves-error" label="Moves — error" {...tvAB}><Center><TVMovesError /></Center></DCArtboard>
        <DCArtboard id="tv-move-detail" label="Move — detail" {...tvAB}><Center><TVMoveDetail /></Center></DCArtboard>
        <DCArtboard id="tv-team" label="Team Builder" {...tvAB}><Center><TVTeam /></Center></DCArtboard>
        <DCArtboard id="tv-matchup" label="Type Matchup" {...tvAB}><Center><TVMatchup /></Center></DCArtboard>
      </DCSection>
    </DesignCanvas>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
