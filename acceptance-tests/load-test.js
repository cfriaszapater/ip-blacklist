const async = require("async");
const fetch = require("node-fetch");

const requests = 1000;
const urls = Array(requests).fill("http://localhost:8080/ips/" + randomIp());
const overallStartTime = new Date().getTime();
async.mapLimit(
  urls,
  5,
  async function(url) {
    const startTime = new Date().getTime();
    const response = await fetch(url);
    response.elapsedTime = new Date().getTime() - startTime;
    return response;
  },
  (err, results) => {
    if (err) throw err;
    // results is now an array of the response bodies

    const overallTime = new Date().getTime() - overallStartTime;

    const requestsWithUnexpectedStatus = results.filter(
      response => response.status !== 200 && response.status !== 204
    );
    const requestsOverMaxThreshold = results.filter(
      response => response.elapsedTime > 200
    );
    const averageLatency =
      results
        .map(response => response.elapsedTime)
        .reduce((accumulator, value) => accumulator + value) / requests;

    console.log("averageLatency: " + averageLatency + "ms");
    console.log(
      "max latency: " +
        results
          .map(response => response.elapsedTime)
          .reduce((accumulator, value) =>
            value > accumulator ? value : accumulator
          ) +
        "ms"
    );
    const requestsPerSecond = (requests * 1000) / overallTime;
    console.log("Rate: " + Math.ceil(requestsPerSecond) + " requests/s");

    if (results.length !== requests) {
      throw new Error(requests - results.length + "requests did not finish.");
    }

    if (requestsWithUnexpectedStatus.length > 0) {
      throw new Error(
        requestsWithUnexpectedStatus.length +
          " requests with unexpected status: " +
          JSON.stringify(requestsWithUnexpectedStatus)
      );
    }

    if (requestsOverMaxThreshold.length > 0) {
      throw new Error(
        requestsOverMaxThreshold.length + " requests over max threshold."
      );
    }

    if (averageLatency > 50) {
      throw new Error("Average latency over threshold: " + averageLatency);
    }

    if (requestsPerSecond < 900) {
      throw new Error(
        "Could not match desired rate of at least 900 requests/s: " +
          requestsPerSecond
      );
    }
  }
);

function randomIp() {
  return "0.0.0.0";
}
