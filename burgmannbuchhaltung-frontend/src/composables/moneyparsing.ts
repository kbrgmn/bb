import BigNumber from "bignumber.js";
import {countCharInString} from "~/composables/stringutils";

const internalDecimalSeparator = '.'
const internalThousandsSeparator = ','

const decimalSeparator = ',' // -> '.'
const thousandsSeparator = '.' // -> ""

const MAX_DECIMAL_PLACES = 5
const MIN_DECIMAL_PLACES = 2

export function checkMoneyFormattingPreconditions(str: string): boolean {
    if (countCharInString(str, decimalSeparator) > 1) {
        return false
    }
    if (countCharInString(str, thousandsSeparator) >= 1) {
        const decimalSplit = str.split(decimalSeparator)

        if (decimalSplit.length > 1) {
            const decimalPart = decimalSplit[1]
            if (countCharInString(decimalPart, thousandsSeparator) >= 1) {
                return false
            }
        }

        const stringWithoutDecimal = decimalSplit[0]

        const thousandsGroups = stringWithoutDecimal.split(thousandsSeparator)
        if (thousandsGroups[0].length > 3 || thousandsGroups[0].length == 0) {
            return false
        }
        for (let idx = 1; idx < thousandsGroups.length; idx++) {
            if (thousandsGroups[idx].length != 3) {
                return false
            }
        }
    }

    if (countCharInString(str, decimalSeparator) == 1) {
        const decimal = str.split(decimalSeparator)[1]
        if (decimal.length < 1) return false
    }

    return true
}

export function parseMoney(str: string | null | undefined): BigNumber | null {
    console.log(`PARSING: >${str}<`)

    if (str === undefined || str === null) {
        return null
    }

    if (str.length === 0) {
        console.log("Empty string, returning 0")
        return BigNumber(0)
    }

    if (!checkMoneyFormattingPreconditions(str)) {
        return null
    }

    console.log(`Preconditions passed: (>${str}<): `, str)


    try {
        // console.log("REPLACE ALL ON: ", str)
        str = str.replaceAll(thousandsSeparator, "")
        str = str.replaceAll(decimalSeparator, internalDecimalSeparator)
    } catch (e) {
        console.log("FAILED ON: ", str, " exception: ", e)
        return null
    }


    return BigNumber(str)
}

const formatter = new Intl.NumberFormat('de-AT', {
    style: 'currency',
    currency: 'EUR',

    // These options are needed to round to whole numbers if that's what you want.
    //minimumFractionDigits: 0, // (this suffices for whole numbers, but will print 2500.10 as $2,500.1)
    maximumFractionDigits: MAX_DECIMAL_PLACES, // (causes 2500.99 to be printed as $2,501)
});

const formatWithoutPrefix = {
    prefix: '',
    decimalSeparator: decimalSeparator,
    groupSeparator: thousandsSeparator,
    groupSize: 3,
    secondaryGroupSize: 0,
    fractionGroupSeparator: ' ',
    fractionGroupSize: 0,
    suffix: ''
}

const formatWithPrefix = {
    prefix: '€ ',
    decimalSeparator: decimalSeparator,
    groupSeparator: thousandsSeparator,
    groupSize: 3,
    secondaryGroupSize: 0,
    fractionGroupSeparator: ' ',
    fractionGroupSize: 0,
    suffix: ''
}

//BigNumber.config({ FORMAT: fmt })

export function formatMoney(num: BigNumber | null | undefined, withPrefix: boolean = true): string | null {
    if (num instanceof BigNumber) {
        let formatted = num.toFormat(MAX_DECIMAL_PLACES, 0, withPrefix ? formatWithPrefix : formatWithoutPrefix).trim()

        for (let i = 0; i < MAX_DECIMAL_PLACES - MIN_DECIMAL_PLACES; i++) {
            if (formatted[formatted.length - 1] === "0") {
                formatted = formatted.substring(0, formatted.length - 1)
            }
        }

        return formatted
    }

    return null

}

export function removeLeadingZeros(s: string): string {
    let firstRelevantCharIdx = 0
    for (firstRelevantCharIdx = 0; firstRelevantCharIdx < s.length; firstRelevantCharIdx++) {
        if (!"0,.".includes(s[firstRelevantCharIdx])) {
            break
        }
    }

    return s.substring(firstRelevantCharIdx)
}
