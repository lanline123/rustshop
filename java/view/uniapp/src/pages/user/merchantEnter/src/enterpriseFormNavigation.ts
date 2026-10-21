type FormRef = { validate: () => Promise<unknown> } | null | undefined;

/** Validate only the form belonging to the currently rendered enterprise step. */
export async function validateEnterpriseStep(step: number, firstForm: FormRef, secondForm: FormRef): Promise<void> {
    if (step === 1 || step === 2) {
        if (firstForm) await firstForm.validate();
        return;
    }

    if (step === 3 && secondForm) {
        await secondForm.validate();
    }
}
