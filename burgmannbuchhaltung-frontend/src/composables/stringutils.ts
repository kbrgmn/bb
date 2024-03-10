export function countCharInString(str: string, char: string): number {
    let count;
    // noinspection StatementWithEmptyBodyJS
    for (let i = (count = 0); i < str.length; count += +(char === str[i++]));
    return count;
}

export function inputKeyCheck(allowedKeys: string, evt: KeyboardEvent) {
    if (!evt.ctrlKey && !evt.altKey && !evt.shiftKey && evt.key.length == 1 && !allowedKeys.includes(evt.key)) {
        console.log("blocked: " + evt.key);
        evt.preventDefault();
    }
}
