# JMeter load test (Budget Manager)

Plan mirrors [student-forum/backend/jmeter/Plan.jmx](https://github.com/Yakush-A/student-forum): async task creation and status polling.

## Prerequisites

- Application running: `mvn spring-boot:run` (port **8080**)
- User with `id=6` exists (`POST /users/register` if needed)
- [Apache JMeter](https://jmeter.apache.org/) 5.x installed

## Run (GUI)

1. Open `jmeter/Plan.jmx`
2. Set **userId** in Test Plan variables if not `6`
3. Run → Start
4. Open **Summary Report** listener

## Run (CLI)

```bash
jmeter -n -t jmeter/Plan.jmx -l jmeter/results.jtl -e -o jmeter/report
```

Open `jmeter/report/index.html` for charts.

## Plan settings

| Setting | Value |
|---------|--------|
| Threads | 50 |
| Ramp-up | 5 s |
| Loops | 5 per thread |
| Requests | `POST /tasks?userId=6` → `GET /tasks/{taskId}?userId=6` |

## What to submit for the lab

Screenshot or export of **Summary Report** with:

- **# Samples**
- **Average** / **90% Line** response time
- **Throughput**
- **Error %** (should be 0% with a running app and valid userId)

Example interpretation: high throughput on `POST /tasks` with low error rate shows the async endpoint accepts concurrent load; `GET` may return `PENDING` or `IN_PROGRESS` while the 10s job runs.
