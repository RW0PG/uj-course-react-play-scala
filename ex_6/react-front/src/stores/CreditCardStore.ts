import {RootStore} from './RootStore';
import {makeAutoObservable} from 'mobx';
import {listUserCreditCards} from '../api/creditCard';
import {CreditCardDb} from "../interfaces/CreditCardDb";


interface ICreditCardStore {
	card: CreditCardDb[]
}

export class CreditCardStore implements ICreditCardStore {
	private rootStore: RootStore | undefined;

	card: CreditCardDb[] = [];
	loaded: boolean = false;

	constructor(rootStore?: RootStore) {
		makeAutoObservable(this)
		this.rootStore = rootStore;
	}

	listCreditCards = async (userId: number) => {
		if (!this.loaded) {
			const userCreditCards = await listUserCreditCards(userId)
			this.loaded = true
			this.card = userCreditCards.data
		}
		const cards = this.card.map((card: CreditCardDb) => {
			const newCreditCard: CreditCardDb = {
				id: card.id,
				userId: card.userId,
				cardholderName: card.cardholderName,
				number: card.number.substr(card.number.length - 4, card.number.length),
				expDate: card.expDate,
				cvcCode: card.cvcCode,
			}
			return newCreditCard
		})
		return [...cards]
	}

	addCard = (card: CreditCardDb) => {
		this.card = [...this.card, card]
	}
}
