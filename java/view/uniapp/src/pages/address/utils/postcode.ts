export const isPostcodeValid = (value: string, required: boolean) => {
    return !required || Boolean(value.trim());
};
