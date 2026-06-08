/* ──────────────────────────────────────────────────────────────
   data.js — sample dataset + type system for the Pokédex mockups.
   Plain globals (window.PDX) so both plain + babel scripts can read it.
   Factual game data — names/types/stats — used as placeholder content;
   real sprites + flavor wired in by Claude Code against PokeAPI.
   ────────────────────────────────────────────────────────────── */
(function () {
  // ── Type color tokens ───────────────────────────────────────
  // Keyed by type id. `dark:true` => badge needs dark text for contrast.
  // Map these 1:1 to Kotlin: object PokemonType { val Fire = Color(0xFFEE8130) … }
  const TYPES = {
    normal:   { name: 'Normal',   color: '#9B9B6E' },
    fire:     { name: 'Fire',     color: '#FF7A33' },
    water:    { name: 'Water',    color: '#4F90F0' },
    electric: { name: 'Electric', color: '#F7CF2E', dark: true },
    grass:    { name: 'Grass',    color: '#62C24A' },
    ice:      { name: 'Ice',      color: '#74CEC0', dark: true },
    fighting: { name: 'Fighting', color: '#D6435A' },
    poison:   { name: 'Poison',   color: '#B04AC3' },
    ground:   { name: 'Ground',   color: '#E0B575', dark: true },
    flying:   { name: 'Flying',   color: '#9C8BF4' },
    psychic:  { name: 'Psychic',  color: '#FB5584' },
    bug:      { name: 'Bug',      color: '#A6B91A' },
    rock:     { name: 'Rock',     color: '#C2B255', dark: true },
    ghost:    { name: 'Ghost',    color: '#7A6BB0' },
    dragon:   { name: 'Dragon',   color: '#7A4CF0' },
    dark:     { name: 'Dark',     color: '#6E5848' },
    steel:    { name: 'Steel',    color: '#9FB0C9', dark: true },
    fairy:    { name: 'Fairy',    color: '#EC8FC5' },
  };

  // ── Stat metadata ───────────────────────────────────────────
  const STATS = [
    { key: 'hp',  label: 'HP',  full: 'Hit Points' },
    { key: 'atk', label: 'ATK', full: 'Attack' },
    { key: 'def', label: 'DEF', full: 'Defense' },
    { key: 'spa', label: 'SP.ATK', full: 'Sp. Attack' },
    { key: 'spd', label: 'SP.DEF', full: 'Sp. Defense' },
    { key: 'spe', label: 'SPD', full: 'Speed' },
  ];
  const STAT_MAX = 255; // theoretical single-stat ceiling, used for StatBar scale

  // ── Pokémon (Gen 1 spread) ──────────────────────────────────
  const P = (dex, name, types, s, extra) => Object.assign({
    dex, name, types,
    stats: { hp: s[0], atk: s[1], def: s[2], spa: s[3], spd: s[4], spe: s[5] },
    total: s.reduce((a, b) => a + b, 0),
  }, extra || {});

  const POKEMON = [
    P(1,  'Bulbasaur',  ['grass', 'poison'], [45, 49, 49, 65, 65, 45]),
    P(3,  'Venusaur',   ['grass', 'poison'], [80, 82, 83, 100, 100, 80]),
    P(4,  'Charmander', ['fire'],            [39, 52, 43, 60, 50, 65]),
    P(6,  'Charizard',  ['fire', 'flying'],  [78, 84, 78, 109, 85, 100], {
      height: 1.7, weight: 90.5,
      flavor: 'A draconic fire-flier whose flame burns hotter the tougher its foe. The tail flame is said to scorch anything it touches.',
      genus: 'Flame Pokémon',
      moves: [
        { name: 'Flamethrower', type: 'fire',    cat: 'special',  power: 90,  acc: 100, pp: 15 },
        { name: 'Air Slash',    type: 'flying',  cat: 'special',  power: 75,  acc: 95,  pp: 15 },
        { name: 'Dragon Claw',  type: 'dragon',  cat: 'physical', power: 80,  acc: 100, pp: 15 },
        { name: 'Fire Spin',    type: 'fire',    cat: 'special',  power: 35,  acc: 85,  pp: 15 },
        { name: 'Wing Attack',  type: 'flying',  cat: 'physical', power: 60,  acc: 100, pp: 35 },
        { name: 'Slash',        type: 'normal',  cat: 'physical', power: 70,  acc: 100, pp: 20 },
        { name: 'Fire Blast',   type: 'fire',    cat: 'special',  power: 110, acc: 85,  pp: 5  },
        { name: 'Heat Wave',    type: 'fire',    cat: 'special',  power: 95,  acc: 90,  pp: 10 },
      ],
      evolution: [
        { dex: 4, name: 'Charmander', types: ['fire'], cond: 'Lv. 16' },
        { dex: 5, name: 'Charmeleon', types: ['fire'], cond: 'Lv. 36' },
        { dex: 6, name: 'Charizard',  types: ['fire', 'flying'], cond: null },
      ],
    }),
    P(7,  'Squirtle',   ['water'],           [44, 48, 65, 50, 64, 43]),
    P(9,  'Blastoise',  ['water'],           [79, 83, 100, 85, 105, 78]),
    P(25, 'Pikachu',    ['electric'],        [35, 55, 40, 50, 50, 90]),
    P(65, 'Alakazam',   ['psychic'],         [55, 50, 45, 135, 95, 120]),
    P(68, 'Machamp',    ['fighting'],        [90, 130, 80, 65, 85, 55]),
    P(94, 'Gengar',     ['ghost', 'poison'], [60, 65, 60, 130, 75, 110]),
    P(130,'Gyarados',   ['water', 'flying'], [95, 125, 79, 60, 100, 81]),
    P(131,'Lapras',     ['water', 'ice'],    [130, 85, 80, 85, 95, 60]),
    P(133,'Eevee',      ['normal'],          [55, 55, 50, 45, 65, 55]),
    P(143,'Snorlax',    ['normal'],          [160, 110, 65, 65, 110, 30]),
    P(149,'Dragonite',  ['dragon', 'flying'],[91, 134, 95, 100, 100, 80]),
    P(150,'Mewtwo',     ['psychic'],         [106, 110, 90, 154, 90, 130]),
  ];
  const byDex = (d) => POKEMON.find((p) => p.dex === d);

  // ── Type effectiveness chart (attacking → defending exceptions) ──
  // Only non-1× entries listed. 0 = immune, 0.5 = resisted, 2 = super.
  const CHART = {
    normal:   { rock: .5, ghost: 0, steel: .5 },
    fire:     { fire: .5, water: .5, grass: 2, ice: 2, bug: 2, rock: .5, dragon: .5, steel: 2 },
    water:    { fire: 2, water: .5, grass: .5, ground: 2, rock: 2, dragon: .5 },
    electric: { water: 2, electric: .5, grass: .5, ground: 0, flying: 2, dragon: .5 },
    grass:    { fire: .5, water: 2, grass: .5, poison: .5, ground: 2, flying: .5, bug: .5, rock: 2, dragon: .5, steel: .5 },
    ice:      { fire: .5, water: .5, grass: 2, ice: .5, ground: 2, flying: 2, dragon: 2, steel: .5 },
    fighting: { normal: 2, ice: 2, poison: .5, flying: .5, psychic: .5, bug: .5, rock: 2, ghost: 0, dark: 2, steel: 2, fairy: .5 },
    poison:   { grass: 2, poison: .5, ground: .5, rock: .5, ghost: .5, steel: 0, fairy: 2 },
    ground:   { fire: 2, electric: 2, grass: .5, poison: 2, flying: 0, bug: .5, rock: 2, steel: 2 },
    flying:   { electric: .5, grass: 2, fighting: 2, bug: 2, rock: .5, steel: .5 },
    psychic:  { fighting: 2, poison: 2, psychic: .5, dark: 0, steel: .5 },
    bug:      { fire: .5, grass: 2, fighting: .5, poison: .5, flying: .5, psychic: 2, ghost: .5, dark: 2, steel: .5, fairy: .5 },
    rock:     { fire: 2, ice: 2, fighting: .5, ground: .5, flying: 2, bug: 2, steel: .5 },
    ghost:    { normal: 0, psychic: 2, ghost: 2, dark: .5 },
    dragon:   { dragon: 2, steel: .5, fairy: 0 },
    dark:     { fighting: .5, psychic: 2, ghost: 2, dark: .5, fairy: .5 },
    steel:    { fire: .5, water: .5, electric: .5, ice: 2, rock: 2, steel: .5, fairy: 2 },
    fairy:    { fire: .5, fighting: 2, poison: .5, dragon: 2, dark: 2, steel: .5 },
  };
  const eff = (atk, def) => (CHART[atk] && CHART[atk][def] != null ? CHART[atk][def] : 1);
  // group all 18 defenders by effectiveness for a chosen attacking type
  function groupEffectiveness(atk) {
    const g = { super: [], normal: [], notvery: [], immune: [] };
    Object.keys(TYPES).forEach((def) => {
      const m = eff(atk, def);
      if (m === 0) g.immune.push(def);
      else if (m > 1) g.super.push(def);
      else if (m < 1) g.notvery.push(def);
      else g.normal.push(def);
    });
    return g;
  }

  // ── Generations ─────────────────────────────────────────────
  // The root selector picks a GENERATION; the dex it opens is the cumulative
  // National Dex #1..dexEnd (everything through that generation).
  // `accent` reuses existing type-color tokens — placeholder card accents,
  // not official brand colors. `versions` are the games bundled in that gen.
  const GENERATIONS = [
    { id: 1, label: 'I',    region: 'Kanto',  dexEnd: 151,  accent: TYPES.fire.color,     versions: ['Red', 'Blue', 'Yellow'] },
    { id: 2, label: 'II',   region: 'Johto',  dexEnd: 251,  accent: TYPES.electric.color, versions: ['Gold', 'Silver', 'Crystal'] },
    { id: 3, label: 'III',  region: 'Hoenn',  dexEnd: 386,  accent: TYPES.water.color,    versions: ['Ruby', 'Sapphire', 'Emerald', 'FireRed', 'LeafGreen'] },
    { id: 4, label: 'IV',   region: 'Sinnoh', dexEnd: 493,  accent: TYPES.ice.color,      versions: ['Diamond', 'Pearl', 'Platinum', 'HeartGold', 'SoulSilver'] },
    { id: 5, label: 'V',    region: 'Unova',  dexEnd: 649,  accent: TYPES.dark.color,     versions: ['Black', 'White', 'Black 2', 'White 2'] },
    { id: 6, label: 'VI',   region: 'Kalos',  dexEnd: 721,  accent: TYPES.fairy.color,    versions: ['X', 'Y', 'Omega Ruby', 'Alpha Sapphire'] },
    { id: 7, label: 'VII',  region: 'Alola',  dexEnd: 809,  accent: TYPES.psychic.color,  versions: ['Sun', 'Moon', 'Ultra Sun', 'Ultra Moon', "Let's Go"] },
    { id: 8, label: 'VIII', region: 'Galar',  dexEnd: 905,  accent: TYPES.fighting.color, versions: ['Sword', 'Shield', 'Brilliant Diamond', 'Shining Pearl', 'Legends: Arceus'] },
    { id: 9, label: 'IX',   region: 'Paldea', dexEnd: 1025, accent: TYPES.dragon.color,   versions: ['Scarlet', 'Violet'] },
  ];
  const genById = (id) => GENERATIONS.find((g) => g.id === id);
  // Sample "active" selection used by the in-app screens.
  const currentGen = 1;
  // Cumulative National Dex #1..dexEnd, sliced from the sample POKEMON set.
  const dexForGen = (gen) => POKEMON.filter((p) => p.dex <= genById(gen).dexEnd);

  // ── Per-generation type system ──────────────────────────────
  // Type roster by era: Gen I = 15 types, Gen II–V = 17 (+Dark,+Steel),
  // Gen VI+ = 18 (+Fairy). Buckets keyed by the first gen of each era.
  const ALL_TYPE_IDS = Object.keys(TYPES); // dark, steel, fairy are the last three
  const TYPE_ROSTER = {
    1: ALL_TYPE_IDS.filter((t) => !['dark', 'steel', 'fairy'].includes(t)), // 15
    2: ALL_TYPE_IDS.filter((t) => t !== 'fairy'),                           // 17 (Gen II–V)
    6: ALL_TYPE_IDS.slice(),                                                // 18 (Gen VI+)
  };
  const eraOf = (gen) => (gen >= 6 ? 6 : gen >= 2 ? 2 : 1);
  const typesForGen = (gen) => TYPE_ROSTER[eraOf(gen)];

  // Historical overrides relative to the modern CHART, transcribed from PokéAPI
  // `type.past_damage_relations` (NOT invented). attacker -> { defender: mult }.
  const CHART_OVERRIDES = {
    1: { // Generation I vs modern
      bug:    { poison: 2 },  // Bug ↔ Poison were mutually super-effective
      poison: { bug: 2 },
      ghost:  { psychic: 0 }, // Psychic was immune to Ghost (the Gen-I quirk)
      ice:    { fire: 1 },    // Fire did not yet resist Ice
    },
    2: { // Generations II–V vs modern: Steel still resisted Ghost & Dark
      ghost: { steel: 0.5 },
      dark:  { steel: 0.5 },
    },
    6: {}, // modern baseline (CHART as-is)
  };

  // gen-aware effectiveness. Types outside the era's roster don't exist → 1×.
  function effGen(atk, def, gen) {
    const era = eraOf(gen);
    const roster = TYPE_ROSTER[era];
    if (!roster.includes(atk) || !roster.includes(def)) return 1;
    const ov = CHART_OVERRIDES[era][atk];
    if (ov && ov[def] != null) return ov[def];
    return CHART[atk] && CHART[atk][def] != null ? CHART[atk][def] : 1;
  }
  function groupEffectivenessGen(atk, gen) {
    const g = { super: [], normal: [], notvery: [], immune: [] };
    typesForGen(gen).forEach((def) => {
      const m = effGen(atk, def, gen);
      if (m === 0) g.immune.push(def);
      else if (m > 1) g.super.push(def);
      else if (m < 1) g.notvery.push(def);
      else g.normal.push(def);
    });
    return g;
  }

  // Defensive view: bucket every attacking type by the multiplier it lands on a defending
  // combo (1–2 types). Dual types stack, so ×4 and ×¼ are possible. `defs` = ['fire','flying'].
  function groupDefenseGen(defs, gen) {
    const g = { quad: [], double: [], neutral: [], half: [], quarter: [], immune: [] };
    if (!defs || !defs.length) return g;
    typesForGen(gen).forEach((atk) => {
      const m = defs.reduce((acc, def) => acc * effGen(atk, def, gen), 1);
      if (m >= 4) g.quad.push(atk);
      else if (m >= 2) g.double.push(atk);
      else if (m === 0) g.immune.push(atk);
      else if (m <= 0.25) g.quarter.push(atk);
      else if (m < 1) g.half.push(atk);
      else g.neutral.push(atk);
    });
    return g;
  }

  // ── Team builder sample ─────────────────────────────────────
  const TEAM = [6, 9, 3, 25, 143, null].map((d) => (d ? byDex(d) : null));

  window.PDX = {
    TYPES, STATS, STAT_MAX, POKEMON, byDex, CHART, eff, groupEffectiveness, TEAM,
    typeIds: Object.keys(TYPES),
    // generations + per-gen type system
    GENERATIONS, genById, currentGen, dexForGen,
    TYPE_ROSTER, eraOf, typesForGen, CHART_OVERRIDES, effGen, groupEffectivenessGen, groupDefenseGen,
  };
})();
