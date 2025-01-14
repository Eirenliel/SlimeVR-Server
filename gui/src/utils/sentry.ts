import * as Sentry from '@sentry/react';
import { error, log } from './logging';
import { useEffect } from 'react';
import {
  createRoutesFromChildren,
  matchRoutes,
  useLocation,
  useNavigationType,
} from 'react-router-dom';

export function getSentryOrCompute(enabled = false) {
  const client = Sentry.getClient();
  if (client) {
    log(`${enabled ? 'Enabled' : 'Disabled'} error logging with Sentry.`);
    client.getOptions().enabled = enabled;
    return client;
  }
  if (!enabled) return;

  const newClient = Sentry.init({
    dsn: 'https://e9ef9f8541352c50cff8600ba520d348@o4507810483535872.ingest.de.sentry.io/4507810579284048',
    integrations: [
      Sentry.reactRouterV6BrowserTracingIntegration({
        useEffect,
        useLocation,
        useNavigationType,
        createRoutesFromChildren,
        matchRoutes,
      }),
      Sentry.browserProfilingIntegration(),
      Sentry.replayIntegration({
        maskAllText: true,
        maskAllInputs: true,
        blockAllMedia: false,
      }),
    ],
    beforeSend: (ev) => (newClient?.getOptions().enabled ? ev : null),
    environment: import.meta.env.MODE,
    release: (__VERSION_TAG__ || __COMMIT_HASH__) + (__GIT_CLEAN__ ? '' : '-dirty'),
    // Tracing
    tracesSampleRate: import.meta.env.PROD ? 0.5 : 1.0, // Capture 50% of the transactions
    // Set profilesSampleRate to 1.0 to profile every transaction.
    // Since profilesSampleRate is relative to tracesSampleRate,
    // the final profiling rate can be computed as tracesSampleRate * profilesSampleRate
    // For example, a tracesSampleRate of 0.5 and profilesSampleRate of 0.5 would
    // results in 25% of transactions being profiled (0.5*0.5=0.25)
    profilesSampleRate: 0.2,
    // Session Replay
    replaysSessionSampleRate: import.meta.env.PROD ? 0.1 : 1.0, // This sets the sample rate at 10%. You may want to change it to 100% while in development and then sample at a lower rate in production.
    replaysOnErrorSampleRate: 1.0, // If you're not already sampling the entire session, change the sample rate to 100% when sampling sessions where errors occur.
    enabled,
  });

  if (!newClient) {
    error('Couldnt initialize Sentry for error logging');
  } else {
    log('Initialized the Sentry client');
  }

  return newClient;
}
