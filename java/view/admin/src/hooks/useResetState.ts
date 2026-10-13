import { ref, reactive } from "vue";

function defaultClone<T>(value: T): T {
    if (typeof value !== "object" || value === null) return value;
    return JSON.parse(JSON.stringify(value));
}

export function useResetReactive<T extends object>(value: T, clone: (value: T) => T = defaultClone) {
    const state = reactive(clone(value)) as T;
    const reset = () => {
        const clonedValue = clone(value);
        Object.keys(state).forEach((key) => {
            if (key in clonedValue) {
                (state as Record<string, any>)[key] = (clonedValue as Record<string, any>)[key];
            } else {
                delete (state as Record<string, any>)[key];
            }
        });
    };
    return [state, reset] as const;
}

export function useResettableRef<T>(value: T, clone: (value: T) => T = defaultClone) {
    const state = ref(clone(value));
    const reset = () => {
        state.value = clone(value);
    };
    return [state, reset] as const;
}
