package com.astralw.core.data.model

/**
 * 账户信息领域模型
 */
data class AccountInfo(
    /** MT5 登录号 */
    val login: Int,
    /** 账户组 */
    val group: String,
    /** 余额 */
    val balance: String,
    /** 信用额度 */
    val credit: String,
    /** 净值 */
    val equity: String,
    /** 已用保证金 */
    val margin: String,
    /** 可用保证金 */
    val freeMargin: String,
    /** 保证金水平 */
    val marginLevel: String,
    /** 杠杆 */
    val leverage: Int,
    /** 货币 */
    val currency: String,
    /** 账户名 */
    val name: String,
)
