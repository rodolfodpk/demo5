import http from 'k6/http';

export default function () {
  const payload = JSON.stringify({ correlationId: `${__VU}${__ITER}-${__VU}${__ITER}`, name: `name${__VU}${__ITER}`, email: `user${__VU}${__ITER}@mail.com`, password: "123456" });
  const params = { headers: { 'Content-Type': 'application/json' } };
  let r = http.post('http://localhost:8081/identity/api/v1/users' , payload, params);
};

