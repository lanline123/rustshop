export const DEFAULT_IMAGE_UPLOAD_MAX_SIZE_MB = 20;

export const isPositiveIntegerImageUploadMaxSize = (value: unknown): boolean => {
    if (value === "" || value === null || value === undefined) {
        return false;
    }

    const numericValue = Number(value);
    return Number.isInteger(numericValue) && numericValue > 0;
};

export const normalizeImageUploadMaxSize = (value: unknown): number =>
    isPositiveIntegerImageUploadMaxSize(value) ? Number(value) : DEFAULT_IMAGE_UPLOAD_MAX_SIZE_MB;

export const imageUploadMaxBytes = (maxSizeMb: unknown): number => normalizeImageUploadMaxSize(maxSizeMb) * 1024 * 1024;

export const isWithinImageUploadLimit = (file: Pick<File, "size">, maxSizeMb: unknown): boolean =>
    file.size <= imageUploadMaxBytes(maxSizeMb);

export const filterFilesByImageUploadLimit = <T extends Pick<File, "size">>(files: T[], maxSizeMb: unknown) => {
    const accepted: T[] = [];
    const rejected: T[] = [];

    files.forEach((file) => (isWithinImageUploadLimit(file, maxSizeMb) ? accepted : rejected).push(file));

    return { accepted, rejected };
};
