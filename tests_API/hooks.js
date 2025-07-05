const hooks = require('hooks');
const { MongoClient, ObjectId } = require('mongodb');
const fetch = require('node-fetch');

// Configuration
const MOCK_SERVER_URL = 'http://users-orders-mock:1080';
const MONGO_ENDPOINT = process.env.MONGO_ENDPOINT || 'mongodb://orders-db:27017/data';

// Données de test MongoDB
const address = [
  { "_id": ObjectId("579f21ae98684924944651bd"), "_class": "sockshop.orders.entities.Address", "number": "69", "street": "Wilson Street", "city": "Hartlepool", "postcode": "TS26 8JU", "country": "United Kingdom" },
  { "_id": ObjectId("579f21ae98684924944651c0"), "_class": "sockshop.orders.entities.Address", "number": "122", "street": "Radstone WayNet", "city": "Northampton", "postcode": "NN2 8NT", "country": "United Kingdom" },
  { "_id": ObjectId("579f21ae98684924944651c3"), "_class": "sockshop.orders.entities.Address", "number": "3", "street": "Radstone Way", "city": "Northampton", "postcode": "NN2 8NT", "country": "United Kingdom" }
];

const card = [
  { "_id": ObjectId("579f21ae98684924944651be"), "_class": "sockshop.orders.entities.Card", "longNum": "8575776807334952", "expires": "08/19", "ccv": "014" },
  { "_id": ObjectId("579f21ae98684924944651c1"), "_class": "sockshop.orders.entities.Card", "longNum": "8918468841895184", "expires": "08/19", "ccv": "597" },
  { "_id": ObjectId("579f21ae98684924944651c4"), "_class": "sockshop.orders.entities.Card", "longNum": "6426429851404909", "expires": "08/19", "ccv": "381" }
];

const cart = [
  { "_id": ObjectId("579f21de98689ebf2bf1cd2f"), "_class": "sockshop.orders.entities.Cart", "customerId": "579f21ae98684924944651bf", "items": [{ "$ref": "item", "$id": ObjectId("579f227698689ebf2bf1cd31") }, { "$ref": "item", "$id": ObjectId("579f22ac98689ebf2bf1cd32") }] },
  { "_id": ObjectId("579f21e298689ebf2bf1cd30"), "_class": "sockshop.orders.entities.Cart", "customerId": "579f21ae98684924944651bfaa", "items": [] }
];

const item = [
  { "_id": ObjectId("579f227698689ebf2bf1cd31"), "_class": "sockshop.orders.entities.Item", "itemId": "819e1fbf-8b7e-4f6d-811f-693534916a8b", "quantity": 20, "unitPrice": 99.0 }
];

const customer = [
  { "_id": "579f21ae98684924944651bf", "_class": "sockshop.orders.entities.Customer", "firstName": "Eve", "lastName": "Berger", "username": "Eve_Berger", "addresses": [{ "$ref": "address", "$id": ObjectId("579f21ae98684924944651bd") }], "cards": [{ "$ref": "card", "$id": ObjectId("579f21ae98684924944651be") }] },
  { "_id": "579f21ae98684924944651c2", "_class": "sockshop.orders.entities.Customer", "firstName": "User", "lastName": "Name", "username": "user", "addresses": [{ "$ref": "address", "$id": ObjectId("579f21ae98684924944651c0") }], "cards": [{ "$ref": "card", "$id": ObjectId("579f21ae98684924944651c1") }] },
  { "_id": "579f21ae98684924944651c5", "_class": "sockshop.orders.entities.Customer", "firstName": "User1", "lastName": "Name1", "username": "user1", "addresses": [{ "$ref": "address", "$id": ObjectId("579f21ae98684924944651c3") }], "cards": [{ "$ref": "card", "$id": ObjectId("579f21ae98684924944651c4") }] }
];

// Configuration des mocks
async function setupMocks() {
  const endpoints = [
    {
      path: '/customers/57a98d98e4b00679b4a830af',
      response: {
        id: "57a98d98e4b00679b4a830af",
        firstName: "Hakim",
        lastName: "Salah",
        email: "hakim@example.com",
        addresses: [{ id: "57a98d98e4b00679b4a830ad" }],
        cards: [{ id: "57a98d98e4b00679b4a830ae" }]
      }
    },
    {
      path: '/addresses/57a98d98e4b00679b4a830ad',
      response: {
        id: "57a98d98e4b00679b4a830ad",
        number: "123",
        street: "Mock Street",
        city: "Mockville",
        postcode: "12345",
        country: "Mockland"
      }
    },
    {
      path: '/cards/57a98d98e4b00679b4a830ae',
      response: {
        id: "57a98d98e4b00679b4a830ae",
        longNum: "1234567812345678",
        expires: "12/30",
        ccv: "123"
      }
    },
    {
      path: '/carts/579f21ae98684924944651bf/items',
      response: [
        { itemId: "1", name: "Product A", quantity: 2, unitPrice: 10.0 },
        { itemId: "2", name: "Product B", quantity: 1, unitPrice: 20.0 }
      ]
    },
    {
      path: '/payment',
      method: 'POST',
      response: {
        authorised: true,
        message: "Payment successful"
      }
    },
    {
      path: '/shipping',
      method: 'POST',
      response: {
        id: "shipment123",
        customerId: "57a98d98e4b00679b4a830af",
        status: "created"
      }
    }
  ];

  for (const endpoint of endpoints) {
    await fetch(`${MOCK_SERVER_URL}/mockserver/expectation`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        httpRequest: { 
          method: endpoint.method || 'GET',
          path: endpoint.path
        },
        httpResponse: {
          statusCode: 200,
          body: JSON.stringify(endpoint.response),
          headers: { "Content-Type": "application/json" }
        }
      })
    });
  }
}

// Initialisation
let db;
hooks.beforeAll(async (transactions, done) => {
  try {
    // Connexion MongoDB
    const client = await MongoClient.connect(MONGO_ENDPOINT, { 
      useNewUrlParser: true, 
      useUnifiedTopology: true 
    });
    db = client.db();
    
    // Configuration des mocks
    await setupMocks();
    done();
  } catch (err) {
    console.error('Initialization error:', err);
    done(err);
  }
});

// Nettoyage
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

// Réinitialisation avant chaque test
hooks.beforeEach((transaction, done) => {
  if (!db) return done(new Error('No DB connection'));
  
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

// Hooks pour POST /orders et GET /orders sans ajouter de simulate flags
hooks.before("/orders > POST", (transaction, done) => {
  transaction.request.headers['Content-Type'] = 'application/json';
  transaction.request.body = JSON.stringify({
    "customer": `${MOCK_SERVER_URL}/customers/57a98d98e4b00679b4a830af`,
    "address": `${MOCK_SERVER_URL}/addresses/57a98d98e4b00679b4a830ad`,
    "card": `${MOCK_SERVER_URL}/cards/57a98d98e4b00679b4a830ae`,
    "items": `${MOCK_SERVER_URL}/carts/579f21ae98684924944651bf/items`
  });
  done();
});

hooks.before("/orders > GET", (transaction, done) => {
  transaction.request.headers["User-Agent"] = "curl/7.43.0";
  transaction.request.headers["Accept"] = "*/*";
  done();
});

// Pas besoin de modifier les corps des requêtes POST pour les scénarios, 
// car les simulate flags sont gérés côté serveur et sont cachés au client
// Donc on ne touche pas aux bodies des requêtes POST ni GET

