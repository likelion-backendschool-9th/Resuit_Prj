import { useEffect, useState } from 'react';

export default function useJobResource(loader, initialData = null) {
  const [state, setState] = useState({ data: initialData, loading: !initialData, error: '' });
  const [version, setVersion] = useState(0);
  useEffect(() => {
    if (initialData && version === 0) {
      setState({ data: initialData, loading: false, error: '' });
      return;
    }
    const controller = new AbortController();
    setState({ data: null, loading: true, error: '' });
    loader(controller.signal).then((data) => {
      if (!controller.signal.aborted) setState({ data, loading: false, error: '' });
    }).catch((error) => {
      if (!controller.signal.aborted) setState({ data: null, loading: false, error: error.message });
    });
    return () => controller.abort();
  }, [loader, initialData, version]);
  return { ...state, retry: () => setVersion((value) => value + 1) };
}
