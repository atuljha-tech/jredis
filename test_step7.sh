#!/bin/bash
R="redis-cli -p 6379"

echo "===== TEST 1: Lists — LPUSH / RPUSH / LRANGE / LLEN ====="
$R DEL mylist > /dev/null
echo -n "LPUSH a b c -> "; $R LPUSH mylist a b c
echo -n "LRANGE 0 -1 -> "; $R LRANGE mylist 0 -1 | tr '\n' ' '; echo
echo -n "RPUSH x y   -> "; $R RPUSH mylist x y
echo -n "LRANGE 0 -1 -> "; $R LRANGE mylist 0 -1 | tr '\n' ' '; echo
echo -n "LLEN        -> "; $R LLEN mylist

echo ""
echo "===== TEST 2: LPOP / RPOP ====="
echo -n "LPOP -> "; $R LPOP mylist
echo -n "RPOP -> "; $R RPOP mylist
echo -n "LRANGE 0 -1 -> "; $R LRANGE mylist 0 -1 | tr '\n' ' '; echo

echo ""
echo "===== TEST 3: Empty list auto-deletes ====="
$R DEL mylist > /dev/null
echo -n "LPUSH solo -> "; $R LPUSH mylist solo
echo -n "LPOP       -> "; $R LPOP mylist
echo -n "EXISTS     -> "; $R EXISTS mylist

echo ""
echo "===== TEST 4: LRANGE edge cases ====="
$R DEL mylist > /dev/null
$R RPUSH mylist a b c d e > /dev/null
echo -n "LRANGE 1 3   -> "; $R LRANGE mylist 1 3 | tr '\n' ' '; echo
echo -n "LRANGE -2 -1 -> "; $R LRANGE mylist -2 -1 | tr '\n' ' '; echo
echo -n "LRANGE 100 200 -> "; $R LRANGE mylist 100 200 | tr '\n' ' '; echo "(empty)"
echo -n "LRANGE 0 -1  -> "; $R LRANGE mylist 0 -1 | tr '\n' ' '; echo

echo ""
echo "===== TEST 5: Sets — uniqueness, SMEMBERS, SCARD, SISMEMBER ====="
$R DEL myset > /dev/null
echo -n "SADD a b c a b -> "; $R SADD myset a b c a b
echo -n "SMEMBERS       -> "; $R SMEMBERS myset | tr '\n' ' '; echo
echo -n "SCARD          -> "; $R SCARD myset
echo -n "SISMEMBER a    -> "; $R SISMEMBER myset a
echo -n "SISMEMBER z    -> "; $R SISMEMBER myset z

echo ""
echo "===== TEST 6: SREM ====="
echo -n "SREM a z -> "; $R SREM myset a z
echo -n "SMEMBERS -> "; $R SMEMBERS myset | tr '\n' ' '; echo

echo ""
echo "===== TEST 7: TYPE finally matters ====="
$R DEL s l st > /dev/null
$R SET s hello > /dev/null
$R LPUSH l x > /dev/null
$R SADD st y > /dev/null
echo -n "TYPE s       -> "; $R TYPE s
echo -n "TYPE l       -> "; $R TYPE l
echo -n "TYPE st      -> "; $R TYPE st
echo -n "TYPE missing -> "; $R TYPE missing

echo ""
echo "===== TEST 8: WRONGTYPE ====="
$R DEL mylist > /dev/null
$R SET mylist notalist > /dev/null
echo -n "LPUSH on string key -> "; $R LPUSH mylist a
$R DEL mylist > /dev/null
$R RPUSH mylist x > /dev/null
echo -n "GET on list key     -> "; $R GET mylist

echo ""
echo "===== TEST 9: Missing keys behave gracefully ====="
$R DEL ghost > /dev/null
echo -n "LLEN ghost        -> "; $R LLEN ghost
echo -n "LPOP ghost        -> "; $R LPOP ghost
echo -n "SMEMBERS ghost    -> "; $R SMEMBERS ghost | tr '\n' ' '; echo "(empty)"
echo -n "SISMEMBER ghost x -> "; $R SISMEMBER ghost x
echo -n "SCARD ghost       -> "; $R SCARD ghost

echo ""
echo "===== TEST 10: TTL on list ====="
$R DEL tlist > /dev/null
$R LPUSH tlist a b c > /dev/null
echo -n "EXPIRE tlist 2 -> "; $R EXPIRE tlist 2
echo -n "TTL tlist      -> "; $R TTL tlist
echo "(waiting 3s...)"
sleep 3
echo -n "EXISTS tlist   -> "; $R EXISTS tlist

echo ""
echo "===== ALL TESTS DONE ====="
