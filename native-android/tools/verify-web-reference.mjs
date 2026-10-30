// Executes only the current scoring handlers in an isolated context. No DOM or localStorage is loaded.
import { readFileSync } from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
const html = readFileSync(new URL('../../index.html', import.meta.url), 'utf8');
function block(marker) {
  const start = html.indexOf(marker);
  assert(start >= 0, `Reference marker missing: ${marker}`);
  const open = html.indexOf('{', start);
  let depth = 1, end = open + 1;
  for (; depth && end < html.length; end++) {
    if (html[end] === '{') depth++;
    if (html[end] === '}') depth--;
  }
  assert.equal(depth, 0);
  return html.slice(open, end);
}
const handlers = Object.fromEntries(['standardHit', 'standardUndo', 'scratch', 'masterAddScratch', 'masterUndo', 'done']
  .map(id => [id, block(`document.getElementById('${id}').onclick=`)]));
const vectors = readFileSync(new URL('../engine/src/test/resources/scoring-vectors.tsv', import.meta.url), 'utf8')
  .split('\n').filter(line => line && !line.startsWith('#'));
for (const line of vectors) {
  const [label, mode, cap, actions, score, shots, pocketed, scratches, done] = line.split(' ');
  const buttons = {};
  const sandbox = {
    document: { getElementById: id => buttons[id] ??= {} },
    save() {}, render() {}, alert() {}, window: { scrollTo() {} }, masterHitOpen: false,
    s: {
      difficulty: mode.toLowerCase(), shotCap: cap === '-' ? (mode === 'MASTER' ? 6 : null) : +cap,
      started: true, draft: 0, draftScratches: 0, finalScratchUsed: false,
      masterShots: 0, masterPocketed: 0, masterRemaining: null, masterActions: [],
      players: [{ active: true }, { active: true }], player: 0, round: 0,
      rounds: [['', '']], scratches: [[0, 0]], masterRounds: [[null, null]],
    },
  };
  const ctx = vm.createContext(sandbox);
  // Load functions directly from index.html; source changes invalidate these anchors.
  const caps = html.match(/const shotCap=.*?;const physicalShots=.*?;/);
  assert(caps, 'Shot cap helpers not found');
  vm.runInContext(caps[0], ctx);
  vm.runInContext(`const activeIdx=()=>s.players.map((p,i)=>p.active?i:-1).filter(i=>i>=0);`, ctx);
  vm.runInContext(`function masterScore()${block('function masterScore()')}
function masterShot(balls)${block('function masterShot(balls)')}`, ctx);
  for (const action of actions === '-' ? [] : actions.split(',')) {
    if (action[0] === 'H' && mode === 'MASTER') vm.runInContext(`masterShot(${+action.slice(1)})`, ctx);
    else {
      const id = action[0] === 'H' ? 'standardHit' : action === 'U' ?
        (mode === 'MASTER' ? 'masterUndo' : 'standardUndo') : (mode === 'MASTER' ? 'masterAddScratch' : 'scratch');
      vm.runInContext(`(e=>${handlers[id]})({currentTarget:{disabled:false}})`, ctx);
    }
  }
  const s = sandbox.s;
  assert.equal(s.draft, +score, `${label}: score`);
  assert.equal(mode === 'MASTER' ? s.masterShots : s.draft - s.draftScratches, +shots, `${label}: shots`);
  assert.equal(s.masterPocketed, +pocketed, `${label}: pocketed`);
  assert.equal(s.draftScratches, +scratches, `${label}: scratches`);
  vm.runInContext(`(()=>${handlers.done})()`, ctx);
  assert.equal(s.rounds[0][0] !== '', done === 'true', `${label}: Done eligibility`);
}
console.log(`${vectors.length} scoring scenarios passed against original index.html handlers.`);
