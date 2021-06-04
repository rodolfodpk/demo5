import http from 'k6/http';

//export let options = {
//  discardResponseBodies: true,
//  scenarios: {
//    contacts: {
//      executor: 'per-vu-iterations',
//      vus: 5000,
//      iterations: 1,
//    },
//  },
//};

export let options = {
  scenarios: {
    constant_request_rate: {
      executor: 'constant-arrival-rate',
      rate: 1000,
      timeUnit: '1s',
      duration: '5s',
      preAllocatedVUs: 500,
      maxVUs: 10000,
    },
  },
};

export default function () {
  const payload = JSON.stringify({ correlationId: `${__VU}${__ITER}-${__VU}${__ITER}`, name: `name${__VU}${__ITER}`, email: `user${__VU}${__ITER}@mail.com`, password: "123456" });
  const params = { headers: { 'Content-Type': 'application/json' } };
  let r = http.post('http://localhost:8081/identity/api/v1/users' , payload, params);

}
