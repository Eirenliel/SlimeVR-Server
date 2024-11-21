import { useEffect, useRef, memo, Fragment } from 'react';

type LogProps = {
  messages: string[]
};

function Log({ messages } : LogProps) {
  const preRef = useRef(null);
  const lastScrollTopRef = useRef(0);

  useEffect(() => {
    const pre = preRef.current;
    const lastScrollTop = lastScrollTopRef.current;
    // Scroll to the latest message if either:
    // - We were looking at the latest message previously; or
    // - The scroll height shrunk, e.g. when logs are cleared due to device reconnecting
    if (pre && (pre.scrollTop >= lastScrollTop || lastScrollTop >= pre.scrollHeight)) {
      pre.scrollTop = pre.scrollHeight;
      lastScrollTopRef.current = pre.scrollTop;
    }
  }, [messages]);

  return (
    <pre ref={preRef}
         style={{
           overflowX: 'auto',
           maxHeight: '300px',
           userSelect: 'text'
         }}
    >
      {messages.length === 0 && (
        <>[No log messages]</>
      )}
      {messages.length > 0 &&
        messages.map((msg, index) => (
          <Fragment key={index}>
              {msg}
              <br />
          </Fragment>
        ))}
    </pre>
  )
}

// Only render if the log messages have actually changed
const LogMemo = memo(Log, (a, b) => {
  return (
    a.messages.length === b.messages.length &&
    a.messages.every((e, i) => b.messages[i] === e));
});

export {
  LogMemo as DeviceLog
};
