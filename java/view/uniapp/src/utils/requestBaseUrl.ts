export const resolveBaseUrl = (envUrl?: string, h5Origin?: string): string => envUrl || h5Origin || "";

let h5Origin: string | undefined = undefined;

// #ifdef H5
h5Origin = location.origin;
// #endif

export const baseUrl = resolveBaseUrl(import.meta.env.VITE_API_URL, h5Origin);
