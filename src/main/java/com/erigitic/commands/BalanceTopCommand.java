/*
 * This file is part of Total Economy, licensed under the MIT License (MIT).
 *
 * Copyright (c) Eric Grandt <https://www.ericgrandt.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.erigitic.commands;

import com.erigitic.config.AccountManager;
import com.erigitic.config.TEAccount;
import com.erigitic.config.TECurrency;
import com.erigitic.main.TotalEconomy;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.*;

public class BalanceTopCommand implements CommandExecutor {
    private Logger logger;
    private TotalEconomy totalEconomy;
    private AccountManager accountManager;

    private PaginationService paginationService = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
    private PaginationList.Builder builder = paginationService.builder();

    public BalanceTopCommand(TotalEconomy totalEconomy) {
        this.totalEconomy = totalEconomy;
        logger = totalEconomy.getLogger();

        accountManager = totalEconomy.getAccountManager();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        ConfigurationNode accountNode = accountManager.getAccountConfig();
        List<Text> accountBalances = new ArrayList<>();
        Map<String, BigDecimal> accountBalancesMap = new HashMap<>();
        Currency defaultCurrency = accountManager.getDefaultCurrency();

        // TODO: Add customization to this (amount of accounts to show).
        accountNode.getChildrenMap().keySet().forEach(accountUUID -> {
            TEAccount playerAccount = (TEAccount) accountManager.getOrCreateAccount(UUID.fromString(accountUUID.toString())).get();
            Text playerName = playerAccount.getDisplayName();

            accountBalancesMap.put(playerName.toPlain(), playerAccount.getBalance(defaultCurrency));
        });

        List<Map.Entry<String, BigDecimal>> unsortedList = new LinkedList<>(accountBalancesMap.entrySet());
        unsortedList.sort((Map.Entry<String, BigDecimal> o1, Map.Entry<String, BigDecimal> o2) -> (o1.getValue()).compareTo(o2.getValue()));

        Collections.reverse(unsortedList);

        unsortedList.forEach(entry -> accountBalances.add(Text.of(TextColors.GRAY, entry.getKey(), ": ", TextColors.GOLD, defaultCurrency.format(entry.getValue()).toPlain())));

        builder.title(Text.of(TextColors.GOLD, "Top Balances"))
                .contents(accountBalances)
                .sendTo(src);

        return CommandResult.success();
    }
}
