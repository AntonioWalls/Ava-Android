import assert from "node:assert/strict";
import { randomUUID } from "node:crypto";

const project = "demo-ava-local";
const db = `projects/${project}/databases/(default)/documents`;
const firestore = `http://127.0.0.1:8080/v1/${db}`;

async function request(url, { method = "GET", token, body, expected = 200 } = {}) {
  const response = await fetch(url, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body ? { "Content-Type": "application/json" } : {}),
    },
    body: body && JSON.stringify(body),
    signal: AbortSignal.timeout(10_000),
  });
  const data = await response.json();
  assert.equal(response.status, expected, data.error?.message ?? `${method} ${url}`);
  return data;
}

async function account(name) {
  const email = `${name.toLowerCase()}-${randomUUID()}@example.test`;
  const { localId: uid, idToken: token } = await request(
    "http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signUp?key=local-emulator-only",
    { method: "POST", body: { email, password: randomUUID(), returnSecureToken: true } },
  );
  const publicProfile = { userid: uid, userName: name, imageUrl: "" };
  await commit(token, [
    update(`Users/${uid}`, { ...publicProfile, fullName: name, phone: "5550000", userMail: email, status: "Offline" }),
    update(`PublicProfiles/${uid}`, publicProfile),
  ]);
  return { uid, token };
}

function value(input) {
  if (input === null) return { nullValue: null };
  if (Array.isArray(input)) return { arrayValue: { values: input.map(value) } };
  if (Number.isInteger(input)) return { integerValue: String(input) };
  if (input instanceof Date) return { timestampValue: input.toISOString() };
  return { stringValue: input };
}

function update(path, fields, { mask, timestamp } = {}) {
  return {
    update: {
      name: `${db}/${path}`,
      fields: Object.fromEntries(Object.entries(fields).map(([key, entry]) => [key, value(entry)])),
    },
    ...(mask ? { updateMask: { fieldPaths: mask } } : {}),
    ...(timestamp ? { updateTransforms: [{ fieldPath: timestamp, setToServerValue: "REQUEST_TIME" }] } : {}),
  };
}

const commit = (token, writes, expected = 200) =>
  request(`${firestore}:commit`, { token, expected, method: "POST", body: { writes } });
const get = (path, token, expected) => request(`${firestore}/${path}`, { token, expected });

const alice = await account("Alice");
const bob = await account("Bob");
const carol = await account("Carol");
await commit(alice.token, [update(`Users/${alice.uid}`, { phone: "5551234" }, { mask: ["phone"] })]);
assert.equal((await get(`Users/${alice.uid}`, alice.token)).fields.phone.stringValue, "5551234");
await get(`Users/${alice.uid}`, bob.token, 403);
await get(`Users/${alice.uid}`, undefined, 403);
const profile = await get(`PublicProfiles/${alice.uid}`, bob.token);
assert.deepEqual(Object.keys(profile.fields).sort(), ["imageUrl", "userName", "userid"]);
console.log("Profile update persisted; the directory only exposes the alias and avatar.");

const post = randomUUID();
const postPath = `Posts/${post}`;
await commit(alice.token, [update(postPath, {
  id: post, userId: alice.uid, userName: "Alice", userProfileImageUrl: null,
  text: "Post de prueba", imageUrl: null, videoUrl: null, timestamp: new Date(),
  likesCount: 0, likedBy: [], commentsCount: 0,
})]);
const comment = randomUUID();
await commit(bob.token, [
  update(`${postPath}/Comments/${comment}`, {
    id: comment, postId: post, userId: bob.uid, userName: "Bob",
    userProfileImageUrl: null, text: "Comentario de prueba", timestamp: new Date(),
  }),
  update(postPath, { commentsCount: 1, lastCommentId: comment }, { mask: ["commentsCount", "lastCommentId"] }),
]);
assert.equal((await get(postPath, alice.token)).fields.commentsCount.integerValue, "1");
await get(`${postPath}/Comments/${comment}`, alice.token);
console.log("Comment and counter were committed together.");

const participants = [alice.uid, bob.uid].sort();
const roomPath = `Messages/${participants.join("-")}`;
await commit(alice.token, [update(roomPath, { participants })]);
const message = randomUUID();
const messagePath = `${roomPath}/chats/${message}`;
await commit(alice.token, [
  update(messagePath, {
    id: message, sender: alice.uid, receiver: bob.uid, message: "Mensaje de prueba",
  }, { timestamp: "time" }),
  update(roomPath, { lastMessageId: message }, { mask: ["lastMessageId"], timestamp: "lastMessageAt" }),
]);
assert.equal((await get(messagePath, bob.token)).fields.message.stringValue, "Mensaje de prueba");
await get(messagePath, carol.token, 403);
await get(messagePath, undefined, 403);
console.log("Only the two chat participants can read their messages.");
