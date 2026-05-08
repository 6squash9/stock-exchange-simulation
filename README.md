# TradeCore

A real-time stock market exchange simulator built in Java that mimics
how an exchange processes orders — from validation to trade execution.

## Architecture
- **Trading Bots** — Produce orders and listen for market updates (Observer Pattern)
- **Order Manager** — Validates and sequences all incoming orders
- **Risk Checker** — Validates orders before they enter the pipeline
- **Sequencer** — Guarantees strict FIFO ordering across the pipeline
- **Matching Engine** — Matches buy/sell orders using price-time priority
- **Market Data Publisher** — Broadcasts trade results back to all bots

## Performance
- ~1200 orders/sec (normal load)
- 60,000+ orders/sec (stress test)
- P50 latency: ~1.2ms
- Stage-wise latency tracking (validation, sequencing, matching, publishing)

## Design Patterns Used
- Observer / Publish-Subscribe
- Strategy Pattern
- Producer-Consumer

## Tech
- Java 21
- Maven

## How to Run
\`\`\`bash
git clone https://github.com/yourusername/tradecore.git
cd tradecore
mvn clean install
mvn exec:java
\`\`\`