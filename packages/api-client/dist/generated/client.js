export const getGetPlatformStatusUrl = () => {
    return `/api/v1/platform/status`;
};
/**
 * @summary Get application status
 */
export const getPlatformStatus = async (options) => {
    const res = await fetch(getGetPlatformStatusUrl(), {
        ...options,
        method: 'GET'
    });
    const body = [204, 205, 304].includes(res.status) ? null : await res.text();
    const data = body ? JSON.parse(body) : {};
    return { data, status: res.status, headers: res.headers };
};
