import http from "k6/http";
import { sleep } from "k6";
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';
import { SharedArray } from "k6/data";

//export let options = {
//  discardResponseBodies: true,
//  scenarios: {
//    contacts: {
//      executor: 'shared-iterations',
//      vus: 2,
//      iterations: 2,
//      maxDuration: '60s',
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

const csvData = new SharedArray("another data name", function() {
    // Load CSV file and parse it using Papa Parse
    return papaparse.parse(open('./users.csv'), { header: false }).data;
});

//var data = open("./users.txt").split(/\r?\n/);

export default function() {
  const params = { headers: { 'Content-Type': 'application/json' } };
  // var employee = data[__ITER % data.length];
  // console.log(`VU ${__VU} on iteration ${__ITER} has employee ID ${employee}...`)
  for (var userPwdPair of csvData) {
      console.log(JSON.stringify(userPwdPair));
    }
  // sleep(1);
};