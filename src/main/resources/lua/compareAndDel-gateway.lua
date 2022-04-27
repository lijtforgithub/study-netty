--[[
比较所在网关服务器 Id 并执行清理,
会操作两张“表”!
1、 先清理 game:user:${用户 Id} 表中的 gateway 字段；
2、 再清理 gateway:id:${网关服务器 Id} 表中的 ${用户 Id}；

XXX 注意: 清理之前先要判断网关服务器 Id

KEYS[1] = 用户 Id, 例如: 1
KEYS[2] = 网关服务器 Id, 例如: 10001
]]

local field_gateway = "gateway"
local key_user = "game:user:" .. KEYS[1]
local key_gateway = "game:gateway:" .. KEYS[2]
local val_userId = KEYS[1]
local val_gatewayId = KEYS[2]

-- 先拿到所在网关服务器 Id
local val = redis.call("hget", key_user, field_gateway)

if (val == val_gatewayId) then
    redis.call("hdel", key_user, field_gateway)
    redis.call("srem", key_gateway, val_userId)
    return 1
end

return 0
