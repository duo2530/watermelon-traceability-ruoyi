#!/usr/bin/env bash
# 西瓜溯源 API 冒烟测试；需后端运行在 :8080，且已临时关闭验证码
set -u
B=${BASE:-http://localhost:8080}
login() {
  post $B/login 'X-Login: 1' "{\"username\":\"$1\",\"password\":\"$2\"}" | python -X utf8 -c "import sys,json;print(json.load(sys.stdin).get('token',''))"
}
# Windows 下 curl 的 -d 参数会经过 ANSI 代码页转换导致中文乱码，请求体统一走 stdin
post() { local url=$1 hdr=$2 body=$3; printf '%s' "$body" | curl -s -X POST "$url" -H "$hdr" -H 'Content-Type: application/json' --data-binary @-; }
j() { python -X utf8 -c "import sys,json;d=json.load(sys.stdin);print($1)"; }

PW='admin123'
AT=$(login admin "$PW"); FT=$(login farmer "$PW")
echo "admin token len ${#AT}, farmer token len ${#FT}"
AH="Authorization: Bearer $AT"; FH="Authorization: Bearer $FT"
CT='Content-Type: application/json'

echo '--- farmer creates plot'
post $B/trace/plot "$FH" '{"plotName":"东台1号棚","areaMu":5.5,"address":"江苏省东台市","soilType":"沙壤土"}' | j "d['code'],d['msg']"
PLOT=$(curl -s "$B/trace/plot/options" -H "$FH" | j "d['data'][0]['plotId']")
echo "plotId=$PLOT"

echo '--- farmer creates batch (auto code + qr)'
R=$(post $B/trace/batch "$FH" "{\"plotId\":$PLOT,\"variety\":\"8424\",\"plantDate\":\"2026-04-01\",\"brandName\":\"东台西瓜\",\"brandDesc\":\"国家地理标志产品\"}")
echo "$R" | j "d['code'],d['data']['batchCode'],d['data']['qrPath']"
BID=$(echo "$R" | j "d['data']['batchId']"); CODE=$(echo "$R" | j "d['data']['batchCode']")

echo '--- farm record online'
post $B/trace/farm "$FH" "{\"batchId\":$BID,\"recordType\":\"PLANT\",\"content\":\"8424 嫁接苗定植\",\"recordTime\":\"2026-04-01 08:00:00\",\"weather\":\"晴\"}" | j "d['code'],d['msg']"

echo '--- qc evaluate & submit'
curl -s "$B/trace/qc/evaluate?batchId=$BID&sugarCenter=12.5&sugarEdge=9.5&fruitShape=1&weightKg=6" -H "$FH" | j "d['gradeName']"
post $B/trace/qc "$FH" "{\"batchId\":$BID,\"sampleNo\":\"S1\",\"sugarCenter\":12.5,\"sugarEdge\":9.5,\"fruitShape\":\"1\",\"weightKg\":6}" | j "d['code'],d['data']['gradeName']"
post $B/trace/qc "$FH" "{\"batchId\":$BID,\"sampleNo\":\"S2\",\"sugarCenter\":40}" | j "d['code'],d['msg']"

echo '--- offline sync (second call must be duplicate)'
BODY="[{\"clientId\":\"c-$CODE-1\",\"type\":\"farm\",\"payload\":{\"batchId\":$BID,\"recordType\":\"FERTILIZE\",\"content\":\"离线施肥\",\"inputName\":\"有机肥\",\"inputAmount\":\"50kg\",\"recordTime\":\"2026-05-10 09:00:00\"}},{\"clientId\":\"c-$CODE-2\",\"type\":\"qc\",\"payload\":{\"batchId\":$BID,\"sugarCenter\":11.2,\"sugarEdge\":8.1,\"fruitShape\":\"2\",\"weightKg\":5}},{\"clientId\":\"c-$CODE-3\",\"type\":\"qc\",\"payload\":{\"batchId\":999999,\"sugarCenter\":11}}]"
post $B/trace/sync/batch "$FH" "$BODY" | j "[(x['result'],x['message']) for x in d['data']]"
post $B/trace/sync/batch "$FH" "$BODY" | j "[(x['result'],x['message']) for x in d['data']]"

echo '--- public trace (anonymous)'
curl -s "$B/trace/public/$CODE" | j "d['code'],d['data']['brand']['name'],len(d['data']['timeline']),d['data']['quality']['avgSugar'],d['data']['quality']['mainGrade']"
curl -s "$B/trace/public/NOPE0000" | j "d['code'],d['msg']"

echo '--- QR image served'
curl -s -o /dev/null -w '%{http_code} %{content_type}\n' "$B/profile/qrcode/$CODE.png"

echo '--- admin sees farmer data, stats, export'
curl -s "$B/trace/batch/list" -H "$AH" | j "d['total']"
curl -s "$B/trace/stats" -H "$AH" | j "d['data']['overview'], d['data']['gradeDistribution']"
curl -s -X POST "$B/trace/archive/export" -H "$AH" -o /tmp/wm_archive.xlsx -w '%{http_code} %{size_download}\n'

echo '--- farmer cannot use admin-only APIs'
post $B/trace/grade "$FH" '{"gradeCode":"X","gradeName":"X"}' | j "d['code']"
curl -s -X POST "$B/trace/archive/export" -H "$FH" | head -c 80; echo
curl -s "$B/trace/stats" -H "$FH" | j "d['code'], d['data']['overview']['qcCount']"
