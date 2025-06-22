const hooks = require('hooks');
const { MongoClient, ObjectId } = require('mongodb');

let db;

const address = [
  { "_id": ObjectId("579f21ae98684924944651bd"), "_class": "works.weave.socks.users.entities.Address", "number": "69", "street": "Wilson Street", "city": "Hartlepool", "postcode": "TS26 8JU", "country": "United Kingdom" },
  { "_id": ObjectId("579f21ae98684924944651c0"), "_class": "works.weave.socks.users.entities.Address", "number": "122", "street": "Radstone WayNet", "city": "Northampton", "postcode": "NN2 8NT", "country": "United Kingdom" },
  { "_id": ObjectId("579f21ae98684924944651c3"), "_class": "works.weave.socks.users.entities.Address", "number": "3", "street": "Radstone Way", "city": "Northampton", "postcode": "NN2 8NT", "country": "United Kingdom" }
];

const card = [
  { "_id": ObjectId("579f21ae98684924944651be"), "_class": "works.weave.socks.users.entities.Card", "longNum": "8575776807334952", "expires": "08/19", "ccv": "014" },
  { "_id": ObjectId("579f21ae98684924944651c1"), "_class": "works.weave.socks.users.entities.Card", "longNum": "8918468841895184", "expires": "08/19", "ccv": "597" },
  { "_id": ObjectId("579f21ae98684924944651c4"), "_class": "works.weave.socks.users.entities.Card", "longNum": "6426429851404909", "expires": "08/19", "ccv": "381" }
];

const cart = [
  { "_id": ObjectId("579f21de98689ebf2bf1cd2f"), "_class": "works.weave.socks.cart.entities.Cart", "customerId": "579f21ae98684924944651bf", "items": [{ "$ref": "item", "$id": ObjectId("579f227698689ebf2bf1cd31") }, { "$ref": "item", "$id": ObjectId("579f22ac98689ebf2bf1cd32") }] },
  { "_id": ObjectId("579f21e298689ebf2bf1cd30"), "_class": "works.weave.socks.cart.entities.Cart", "customerId": "579f21ae98684924944651bfaa", "items": [] }
];

const item = [
  { "_id": ObjectId("579f227698689ebf2bf1cd31"), "_class": "works.weave.socks.cart.entities.Item", "itemId": "819e1fbf-8b7e-4f6d-811f-693534916a8b", "quantity": 20, "unitPrice": 99.0 }
];

const customer = [
  { "_id": "579f21ae98684924944651bf", "_class": "works.weave.socks.users.entities.Customer", "firstName": "Eve", "lastName": "Berger", "username": "Eve_Berger", "addresses": [{ "$ref": "address", "$id": ObjectId("579f21ae98684924944651bd") }], "cards": [{ "$ref": "card", "$id": ObjectId("579f21ae98684924944651be") }] },
  { "_id": "579f21ae98684924944651c2", "_class": "works.weave.socks.users.entities.Customer", "firstName": "User", "lastName": "Name", "username": "user", "addresses": [{ "$ref": "address", "$id": ObjectId("579f21ae98684924944651c0") }], "cards": [{ "$ref": "card", "$id": ObjectId("579f21ae98684924944651c1") }] },
  { "_id": "579f21ae98684924944651c5", "_class": "works.weave.socks.users.entities.Customer", "firstName": "User1", "lastName": "Name1", "username": "user1", "addresses": [{ "$ref": "address", "$id": ObjectId("579f21ae98684924944651c3") }], "cards": [{ "$ref": "card", "$id": ObjectId("579f21ae98684924944651c4") }] }
];

// Connexion à MongoDB avant tous les tests
hooks.beforeAll((transactions, done) => {
  const mongoEndpoint = process.env.MONGO_ENDPOINT || 'mongodb://localhost:32769/data';
  MongoClient.connect(mongoEndpoint, { useNewUrlParser: true, useUnifiedTopology: true }, (err, client) => {
    if (err) {
      console.error('MongoDB connection error:', err);
      done(err);
      return;
    }
    db = client.db();
    done();
  });
});

// Fermeture de la connexion après tous les tests
hooks.afterAll((transactions, done) => {
  if (db) {
    db.dropDatabase()
      .then(() => done())
      .catch(e => {
        console.error('Error dropping DB:', e);
        done(e);
      });
  } else {
    done();
  }
});

// Réinitialisation de la base avant chaque test
hooks.beforeEach((transaction, done) => {
  if (!db) {
    done(new Error('No DB connection'));
    return;
  }
  db.dropDatabase()
    .then(() => Promise.all([
      db.collection('customer').insertMany(customer),
      db.collection('card').insertMany(card),
      db.collection('cart').insertMany(cart),
      db.collection('address').insertMany(address),
      db.collection('item').insertMany(item)
    ]))
    .then(() => done())
    .catch(err => {
      console.error('Error seeding DB:', err);
      done(err);
    });
});

// Préparation de la requête POST /orders classique
hooks.before("/orders > POST", (transaction, done) => {
  transaction.request.headers['Content-Type'] = 'application/json';
  transaction.request.body = JSON.stringify({
    "customer": "http://users-orders-mock:80/customers/57a98d98e4b00679b4a830af",
    "address": "http://users-orders-mock:80/addresses/57a98d98e4b00679b4a830ad",
    "card": "http://users-orders-mock:80/cards/57a98d98e4b00679b4a830ae",
    "items": "http://users-orders-mock:80/carts/579f21ae98684924944651bf/items"
  });
  done();
});

// Préparation de la requête GET /orders
hooks.before("/orders > GET", (transaction, done) => {
  transaction.request.headers["User-Agent"] = "curl/7.43.0";
  transaction.request.headers["Accept"] = "*/*";
  done();
});

// Fonction pour générer les payloads de test simulés
const simulateTest = (scenario) => ({
  "customer": "http://users-orders-mock:80/customers/57a98d98e4b00679b4a830af",
  "address": "http://users-orders-mock:80/addresses/57a98d98e4b00679b4a830ad",
  "card": "http://users-orders-mock:80/cards/57a98d98e4b00679b4a830ae",
  "items": "http://users-orders-mock:80/carts/579f21ae98684924944651bf/items",
  "simulate": scenario
});

// Générer un hook par scénario simulé POST /orders {scenario}
["latency", "cpu", "leak", "thread", "deadlock", "error"].forEach(scenario => {
  hooks.before(`/orders > POST ${scenario}`, function (transaction, done) {
    transaction.name = `/orders > POST ${scenario}`;
    transaction.request.headers['Content-Type'] = 'application/json';
    transaction.request.body = JSON.stringify(simulateTest(scenario));
    done();
  });
});
