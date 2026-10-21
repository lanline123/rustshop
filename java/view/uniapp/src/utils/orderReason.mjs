export const buildCancelOrderPayload = (id, reason) => {
    const description = String(reason ?? "").trim();
    return description ? { id, description } : { id };
};
export const keepValidSelection = (selected, reasons) => (reasons.includes(selected) ? selected : "");
