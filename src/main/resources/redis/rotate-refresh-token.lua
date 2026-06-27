local currentTokenId = redis.call('GET', KEYS[1])

if not currentTokenId then
    return 0
end

if currentTokenId ~= ARGV[1] then
    redis.call('DEL', KEYS[1])
    return -1
end

redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3])
return 1
