import React, { useState } from 'react';
import axios from 'axios';
const PokemonScreen = () => {

        // status
        const [name, setName] = useState('');
        const [errorMessage, setErrorMessage] = useState('');
        const [pokemonName, setPokemonName] = useState('');
        const [pokemonDescription, setPokemonDescription] = useState('');
        const [isLegendary, setIsLegendary] = useState('');

        // search history
        const [previousSearch, setPreviousSearch] = useState([])


        const handleSubmit = (e) => {

            console.log('http://localhost:8000/pokemon/' + name);

            // request to server
            axios.get('http://localhost:8000/pokemon/' + name).then(res => {
                console.log(res.data);
                var pokemon = res.data;
                setPokemonName(pokemon.name);
                setPokemonDescription(pokemon.description);
                setIsLegendary(pokemon.is_legendary);
                if(!pokemon.name){
                    setErrorMessage('Pokemon not Found');
                } else {
                    setErrorMessage('');
                    setPreviousSearch([pokemon.name, ...previousSearch.slice(0, 4)]);
                }
                console.log(pokemonName);
                console.log(pokemonDescription);
                console.log(isLegendary);


            }).catch(error => {
                    setErrorMessage("Error: " + error.message);
                  });
            e.preventDefault();
        }

        return(
        <div>
            <h1>Pokemon Search App</h1>
            <form onSubmit = {handleSubmit}>
                <input onChange = {(e) => setName(e.target.value)} value = {name}></input>
                <button type = 'submit'>Click to submit</button>
            </form>
            <h2>Pokemon Name</h2>
            <h3 style={{fontStyle: 'italic'}}>{pokemonName}</h3>
            <h2>Pokemon Description</h2>
            <h3 style={{fontStyle: 'italic'}}>{pokemonDescription}</h3>
            <h2>Pokemon Is Legendary</h2>
            <h3 style={{fontStyle: 'italic'}}>{isLegendary}</h3>
            <h2 style={{ color: 'red' }}>{errorMessage}</h2>

            <div style={{ marginTop: '5rem' }} >
                <h2>Previous Searched Pokemons</h2>
                  {previousSearch.map(pSearch => <p>{pSearch}</p>)}
                    </div>
            </div>
        );

    }

export default PokemonScreen;