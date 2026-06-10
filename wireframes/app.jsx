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
      {/* Design system on top — shared tokens & components drive every view below. */}
      <DCSection id="foundations" title="Foundations" subtitle="Shared tokens & components — core/ui-common">
        <DCArtboard id="tokens" label="Tokens & Components" {...foundAB}>
          <Center><Foundations /></Center>
        </DCArtboard>
      </DCSection>

      {/* Each section below pairs the Phone and TV artboards for one view, side by side. */}
      <DCSection id="root-select" title="Root · Version Select" subtitle="Phone + TV · pick a generation first — sets the cumulative National Dex + era type chart for the whole app">
        <DCArtboard id="vs-phone" label="Phone — choose generation" {...phoneAB}><Center><PhoneVersionSelect /></Center></DCArtboard>
        <DCArtboard id="vs-phone-loading" label="Phone — loading (skeleton)" {...phoneAB}><Center><PhoneVersionSelectLoading /></Center></DCArtboard>
        <DCArtboard id="vs-tv" label="TV — choose generation (D-pad)" {...tvAB}><Center><TVVersionSelect /></Center></DCArtboard>
      </DCSection>

      <DCSection id="list-browse" title="Pokédex List · Browse" subtitle="Phone (Material 3 · bottom nav) + TV (leanback grid · nav rail) · ViewModel states">
        <DCArtboard id="list" label="Phone — list loaded" {...phoneAB}><Center><PhoneList /></Center></DCArtboard>
        <DCArtboard id="list-loading" label="Phone — loading (skeleton)" {...phoneAB}><Center><PhoneListLoading /></Center></DCArtboard>
        <DCArtboard id="list-error" label="Phone — error" {...phoneAB}><Center><PhoneListError /></Center></DCArtboard>
        <DCArtboard id="tv-browse" label="TV — browse grid" {...tvAB}><Center><TVBrowse /></Center></DCArtboard>
        <DCArtboard id="tv-browse-error" label="TV — browse error" {...tvAB}><Center><TVBrowseError /></Center></DCArtboard>
      </DCSection>

      <DCSection id="detail" title="Pokémon Detail" subtitle="Phone (Console Card · Stats / Moves / About / Evolution tabs) + TV detail">
        <DCArtboard id="detail-stats" label="Phone — stats tab" {...phoneAB}><Center><DetailB /></Center></DCArtboard>
        <DCArtboard id="detail-moves" label="Phone — moves tab" {...phoneAB}><Center><DetailMoves /></Center></DCArtboard>
        <DCArtboard id="detail-about" label="Phone — about tab" {...phoneAB}><Center><DetailAbout /></Center></DCArtboard>
        <DCArtboard id="detail-evo" label="Phone — evolution tab" {...phoneAB}><Center><DetailEvolution /></Center></DCArtboard>
        <DCArtboard id="tv-detail" label="TV — detail" {...tvAB}><Center><TVDetail /></Center></DCArtboard>
      </DCSection>

      <DCSection id="items" title="Items Dictionary" subtitle="Phone + TV · searchable item list · generation-scoped · select a row for detail">
        <DCArtboard id="items-loaded" label="Phone — items loaded" {...phoneAB}><Center><PhoneItems /></Center></DCArtboard>
        <DCArtboard id="items-loading" label="Phone — loading (skeleton)" {...phoneAB}><Center><PhoneItemsLoading /></Center></DCArtboard>
        <DCArtboard id="items-error" label="Phone — error" {...phoneAB}><Center><PhoneItemsError /></Center></DCArtboard>
        <DCArtboard id="item-detail" label="Phone — item detail" {...phoneAB}><Center><ItemDetail /></Center></DCArtboard>
        <DCArtboard id="tv-items" label="TV — items loaded" {...tvAB}><Center><TVItems /></Center></DCArtboard>
        <DCArtboard id="tv-items-loading" label="TV — loading" {...tvAB}><Center><TVItemsLoading /></Center></DCArtboard>
        <DCArtboard id="tv-items-error" label="TV — error" {...tvAB}><Center><TVItemsError /></Center></DCArtboard>
        <DCArtboard id="tv-item-detail" label="TV — item detail" {...tvAB}><Center><TVItemDetail /></Center></DCArtboard>
      </DCSection>

      <DCSection id="moves" title="Moves Dictionary" subtitle="Phone + TV · searchable move list · generation-scoped · select a row for detail">
        <DCArtboard id="moves-loaded" label="Phone — moves loaded" {...phoneAB}><Center><PhoneMoves /></Center></DCArtboard>
        <DCArtboard id="moves-loading" label="Phone — loading (skeleton)" {...phoneAB}><Center><PhoneMovesLoading /></Center></DCArtboard>
        <DCArtboard id="moves-error" label="Phone — error" {...phoneAB}><Center><PhoneMovesError /></Center></DCArtboard>
        <DCArtboard id="move-detail" label="Phone — move detail" {...phoneAB}><Center><MoveDetail /></Center></DCArtboard>
        <DCArtboard id="tv-moves" label="TV — moves loaded" {...tvAB}><Center><TVMoves /></Center></DCArtboard>
        <DCArtboard id="tv-moves-loading" label="TV — loading" {...tvAB}><Center><TVMovesLoading /></Center></DCArtboard>
        <DCArtboard id="tv-moves-error" label="TV — error" {...tvAB}><Center><TVMovesError /></Center></DCArtboard>
        <DCArtboard id="tv-move-detail" label="TV — move detail" {...tvAB}><Center><TVMoveDetail /></Center></DCArtboard>
      </DCSection>

      <DCSection id="team-matchup" title="Team & Matchup" subtitle="Phone + TV · Team Builder + Type Matchup Calculator">
        <DCArtboard id="team" label="Phone — team builder" {...phoneAB}><Center><PhoneTeam /></Center></DCArtboard>
        <DCArtboard id="matchup" label="Phone — type matchup" {...phoneAB}><Center><PhoneMatchup /></Center></DCArtboard>
        <DCArtboard id="tv-team" label="TV — team builder" {...tvAB}><Center><TVTeam /></Center></DCArtboard>
        <DCArtboard id="tv-matchup" label="TV — type matchup" {...tvAB}><Center><TVMatchup /></Center></DCArtboard>
      </DCSection>
    </DesignCanvas>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
