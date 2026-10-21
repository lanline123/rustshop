#!/usr/bin/env node
import readline from 'node:readline';
import { createConnector } from './connector.mjs';

const connector = createConnector({
  baseUrl: process.env.TIGSHOP_BASE_URL,
  apiKey: process.env.TIGSHOP_MCP_API_KEY
});

const input = readline.createInterface({ input: process.stdin, crlfDelay: Infinity });
for await (const line of input) {
  if (!line.trim()) continue;
  let request;
  try { request = JSON.parse(line); } catch {
    process.stdout.write(`${JSON.stringify({ jsonrpc: '2.0', id: null, error: { code: -32700, message: 'Parse error' } })}\n`);
    continue;
  }
  const response = await connector.handle(request);
  if (response) process.stdout.write(`${JSON.stringify(response)}\n`);
}
